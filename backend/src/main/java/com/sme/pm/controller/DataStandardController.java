package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.DataStandard;
import com.sme.pm.service.IDataStandardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/data-standards")
public class DataStandardController {

    private final IDataStandardService dataStandardService;

    public DataStandardController(IDataStandardService dataStandardService) {
        this.dataStandardService = dataStandardService;
    }

    @GetMapping
    public Result<List<DataStandard>> list(@RequestParam(required = false) String type) {
        if (type != null && !type.isEmpty()) {
            return Result.success(dataStandardService.getByType(type));
        }
        return Result.success(dataStandardService.getAll());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        return Result.success(dataStandardService.getDetail(id));
    }

    @PostMapping
    public Result<DataStandard> create(@RequestBody DataStandard dataStandard) {
        return Result.success(dataStandardService.create(dataStandard));
    }

    @PutMapping("/{id}")
    public Result<DataStandard> update(@PathVariable Long id, @RequestBody DataStandard dataStandard) {
        return Result.success(dataStandardService.update(id, dataStandard));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dataStandardService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/share")
    public Result<Void> share(@PathVariable Long id, @RequestBody Map<String, Object> shareParams) {
        dataStandardService.share(id, shareParams);
        return Result.success();
    }
}
