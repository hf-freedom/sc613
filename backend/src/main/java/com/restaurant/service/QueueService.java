package com.restaurant.service;

import com.restaurant.entity.QueueItem;
import com.restaurant.entity.Statistics;
import com.restaurant.entity.Table;
import com.restaurant.enums.QueueStatus;
import com.restaurant.enums.TableStatus;
import com.restaurant.enums.TableType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class QueueService {
    private final Map<String, QueueItem> queueMap = new ConcurrentHashMap<>();
    private final Map<TableType, Integer> queueNumberCounters = new ConcurrentHashMap<>();
    private final int CONFIRM_TIMEOUT_SECONDS = 180;

    @Autowired
    private TableService tableService;

    @Autowired
    private StatisticsService statisticsService;

    public QueueService() {
        for (TableType type : TableType.values()) {
            queueNumberCounters.put(type, 0);
        }
    }

    public QueueItem takeQueue(int peopleCount) {
        TableType tableType = TableType.getByPeopleCount(peopleCount);
        int number = queueNumberCounters.merge(tableType, 1, Integer::sum);
        
        String queueId = UUID.randomUUID().toString();
        String prefix = getQueuePrefix(tableType);
        String queueNumber = prefix + String.format("%03d", number);

        QueueItem item = new QueueItem();
        item.setQueueId(queueId);
        item.setQueueNumber(queueNumber);
        item.setTableType(tableType);
        item.setPeopleCount(peopleCount);
        item.setStatus(QueueStatus.WAITING);
        item.setCreateTime(LocalDateTime.now());
        item.setConfirmTimeoutSeconds(CONFIRM_TIMEOUT_SECONDS);

        queueMap.put(queueId, item);
        statisticsService.incrementTotalQueues();
        return item;
    }

    private String getQueuePrefix(TableType type) {
        switch (type) {
            case SMALL: return "A";
            case MEDIUM: return "B";
            case LARGE: return "C";
            case EXTRA_LARGE: return "D";
            default: return "X";
        }
    }

    public QueueItem callNextQueue(TableType tableType) {
        List<QueueItem> waitingQueue = queueMap.values().stream()
                .filter(item -> item.getTableType() == tableType && item.getStatus() == QueueStatus.WAITING)
                .sorted(Comparator.comparing(QueueItem::getCreateTime))
                .collect(Collectors.toList());

        if (waitingQueue.isEmpty()) {
            return null;
        }

        QueueItem item = waitingQueue.get(0);
        item.setStatus(QueueStatus.CALLED);
        item.setCallTime(LocalDateTime.now());
        return item;
    }

    public boolean confirmQueue(String queueId) {
        QueueItem item = queueMap.get(queueId);
        if (item == null || item.getStatus() != QueueStatus.CALLED) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        long elapsedSeconds = Duration.between(item.getCallTime(), now).getSeconds();
        
        if (elapsedSeconds > item.getConfirmTimeoutSeconds()) {
            item.setStatus(QueueStatus.SKIPPED);
            return false;
        }

        item.setStatus(QueueStatus.CONFIRMED);
        item.setConfirmTime(now);
        
        long waitSeconds = Duration.between(item.getCreateTime(), now).getSeconds();
        statisticsService.addWaitTime(waitSeconds);

        Table table = tableService.assignTableToQueue(item.getTableType(), queueId);
        return table != null;
    }

    public void checkTimeouts() {
        LocalDateTime now = LocalDateTime.now();
        for (QueueItem item : queueMap.values()) {
            if (item.getStatus() == QueueStatus.CALLED) {
                long elapsedSeconds = Duration.between(item.getCallTime(), now).getSeconds();
                if (elapsedSeconds > item.getConfirmTimeoutSeconds()) {
                    item.setStatus(QueueStatus.SKIPPED);
                    callNextQueue(item.getTableType());
                }
            }
        }
    }

    public List<QueueItem> getAllQueues() {
        return new ArrayList<>(queueMap.values());
    }

    public Map<TableType, List<QueueItem>> getQueueByType() {
        Map<TableType, List<QueueItem>> result = new HashMap<>();
        for (TableType type : TableType.values()) {
            List<QueueItem> list = queueMap.values().stream()
                    .filter(item -> item.getTableType() == type && item.getStatus() == QueueStatus.WAITING)
                    .sorted(Comparator.comparing(QueueItem::getCreateTime))
                    .collect(Collectors.toList());
            result.put(type, list);
        }
        return result;
    }

    public QueueItem getQueueById(String queueId) {
        return queueMap.get(queueId);
    }
}
