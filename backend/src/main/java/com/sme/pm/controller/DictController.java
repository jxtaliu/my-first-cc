package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.dto.DictCodeDTO;
import com.sme.pm.entity.DictCode;
import com.sme.pm.entity.DictType;
import com.sme.pm.service.DictService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dicts")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping
    public Result<List<DictType>> getAllTypes() {
        return Result.success(dictService.getAllTypes());
    }

    @GetMapping("/codes/{type}")
    public Result<List<DictCodeDTO>> getCodesByType(@PathVariable String type) {
        return Result.success(dictService.getCodesByType(type));
    }

    @GetMapping("/codes/{type}/{code}")
    public Result<DictCodeDTO> getCode(@PathVariable String type, @PathVariable String code) {
        DictCodeDTO dictCode = dictService.getCode(type, code);
        if (dictCode == null) {
            return Result.success(null);
        }
        return Result.success(dictCode);
    }

    @PostMapping("/refresh")
    public Result<Void> refreshCache() {
        dictService.refreshCache();
        return Result.success();
    }

    // DictType Management

    @GetMapping("/types")
    public Result<List<DictType>> getAllTypesForManagement() {
        return Result.success(dictService.getAllTypes());
    }

    @GetMapping("/types/{id}")
    public Result<DictType> getTypeById(@PathVariable Long id) {
        DictType type = dictService.getTypeById(id);
        if (type == null) {
            return Result.success(null);
        }
        return Result.success(type);
    }

    @PostMapping("/types")
    public Result<DictType> createType(@RequestBody DictType dictType) {
        return Result.success(dictService.createType(dictType));
    }

    @PutMapping("/types/{id}")
    public Result<Void> updateType(@PathVariable Long id, @RequestBody DictType dictType) {
        dictType.setId(id);
        dictService.updateType(dictType);
        return Result.success();
    }

    @DeleteMapping("/types/{id}")
    public Result<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return Result.success();
    }

    // DictCode Management

    @GetMapping("/items")
    public Result<List<DictCode>> getAllItems(@RequestParam(required = false) Long typeId) {
        if (typeId != null) {
            return Result.success(dictService.getItemsByTypeId(typeId));
        }
        return Result.success(dictService.getAllItems());
    }

    @GetMapping("/items/{id}")
    public Result<DictCode> getItemById(@PathVariable Long id) {
        DictCode item = dictService.getItemById(id);
        if (item == null) {
            return Result.success(null);
        }
        return Result.success(item);
    }

    @PostMapping("/items")
    public Result<DictCode> createItem(@RequestBody DictCode dictCode) {
        return Result.success(dictService.createItem(dictCode));
    }

    @PutMapping("/items/{id}")
    public Result<Void> updateItem(@PathVariable Long id, @RequestBody DictCode dictCode) {
        dictCode.setId(id);
        dictService.updateItem(dictCode);
        return Result.success();
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        dictService.deleteItem(id);
        return Result.success();
    }
}
