package com.restaurant.controller;

import com.restaurant.common.Result;
import com.restaurant.entity.Table;
import com.restaurant.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/table")
@CrossOrigin
public class TableController {
    @Autowired
    private TableService tableService;

    @GetMapping("/list")
    public Result<List<Table>> getAllTables() {
        return Result.success(tableService.getAllTables());
    }

    @GetMapping("/{tableId}")
    public Result<Table> getTableById(@PathVariable String tableId) {
        return Result.success(tableService.getTableById(tableId));
    }

    @GetMapping("/byQueue/{queueId}")
    public Result<Table> getTableByQueueId(@PathVariable String queueId) {
        return Result.success(tableService.getTableByQueueId(queueId));
    }

    @PostMapping("/clean")
    public Result<Boolean> cleanTable(@RequestParam String tableId) {
        boolean success = tableService.cleanTable(tableId);
        return Result.success(success);
    }
}
