package com.restaurant.service;

import com.restaurant.entity.Table;
import com.restaurant.enums.TableStatus;
import com.restaurant.enums.TableType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TableService {
    private final Map<String, Table> tableMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initTables() {
        createTable("S01", TableType.SMALL);
        createTable("S02", TableType.SMALL);
        createTable("S03", TableType.SMALL);
        createTable("M01", TableType.MEDIUM);
        createTable("M02", TableType.MEDIUM);
        createTable("M03", TableType.MEDIUM);
        createTable("L01", TableType.LARGE);
        createTable("L02", TableType.LARGE);
        createTable("XL01", TableType.EXTRA_LARGE);
    }

    private void createTable(String tableNumber, TableType type) {
        Table table = new Table();
        table.setTableId(UUID.randomUUID().toString());
        table.setTableNumber(tableNumber);
        table.setTableType(type);
        table.setStatus(TableStatus.AVAILABLE);
        tableMap.put(table.getTableId(), table);
    }

    public Table assignTableToQueue(TableType tableType, String queueId) {
        List<Table> availableTables = tableMap.values().stream()
                .filter(t -> t.getTableType() == tableType && t.getStatus() == TableStatus.AVAILABLE)
                .collect(Collectors.toList());

        if (availableTables.isEmpty()) {
            return null;
        }

        Table table = availableTables.get(0);
        table.setStatus(TableStatus.OCCUPIED);
        table.setCurrentQueueId(queueId);
        table.setOccupyTime(LocalDateTime.now());
        return table;
    }

    public boolean checkoutTable(String tableId) {
        Table table = tableMap.get(tableId);
        if (table == null || table.getStatus() != TableStatus.OCCUPIED) {
            return false;
        }
        table.setStatus(TableStatus.CLEANING);
        table.setCheckoutTime(LocalDateTime.now());
        return true;
    }

    public boolean cleanTable(String tableId) {
        Table table = tableMap.get(tableId);
        if (table == null || table.getStatus() != TableStatus.CLEANING) {
            return false;
        }
        table.setStatus(TableStatus.AVAILABLE);
        table.setCurrentQueueId(null);
        table.setOccupyTime(null);
        table.setCheckoutTime(null);
        return true;
    }

    public List<Table> getAllTables() {
        return new ArrayList<>(tableMap.values());
    }

    public Table getTableById(String tableId) {
        return tableMap.get(tableId);
    }

    public Table getTableByQueueId(String queueId) {
        return tableMap.values().stream()
                .filter(t -> queueId.equals(t.getCurrentQueueId()))
                .findFirst()
                .orElse(null);
    }
}
