package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.dto.DictCodeDTO;
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

    @GetMapping("/{type}")
    public Result<List<DictCodeDTO>> getCodesByType(@PathVariable String type) {
        return Result.success(dictService.getCodesByType(type));
    }

    @GetMapping("/{type}/{code}")
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
}
