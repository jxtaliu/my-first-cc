package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.dto.DictCodeDTO;
import com.sme.pm.entity.DictCode;
import com.sme.pm.entity.DictType;
import com.sme.pm.service.DictService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典控制器
 * 提供数据字典管理相关API，包括字典类型CRUD、字典项CRUD、缓存刷新等
 *
 * 用法：
 * - GET /api/dicts - 获取所有字典类型
 * - GET /api/dicts/codes/{type} - 获取指定类型的字典项
 * - POST /api/dicts/refresh - 刷新字典缓存
 * - GET /api/dicts/types - 获取字典类型列表（管理用）
 * - POST /api/dicts/types - 创建字典类型
 * - GET /api/dicts/items - 获取字典项列表
 * - POST /api/dicts/items - 创建字典项
 */
@RestController
@RequestMapping("/api/dicts")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    /**
     * 获取所有字典类型
     * @return 字典类型列表
     */
    @GetMapping
    public Result<List<DictType>> getAllTypes() {
        return Result.success(dictService.getAllTypes());
    }

    /**
     * 获取指定类型的字典项
     * @param type 字典类型
     * @return 字典项列表
     */
    @GetMapping("/codes/{type}")
    public Result<List<DictCodeDTO>> getCodesByType(@PathVariable String type) {
        return Result.success(dictService.getCodesByType(type));
    }

    /**
     * 获取指定类型和代码的字典项
     * @param type 字典类型
     * @param code 字典代码
     * @return 字典项信息
     */
    @GetMapping("/codes/{type}/{code}")
    public Result<DictCodeDTO> getCode(@PathVariable String type, @PathVariable String code) {
        DictCodeDTO dictCode = dictService.getCode(type, code);
        if (dictCode == null) {
            return Result.success(null);
        }
        return Result.success(dictCode);
    }

    /**
     * 刷新字典缓存
     * @return 成功返回空结果
     */
    @PostMapping("/refresh")
    public Result<Void> refreshCache() {
        dictService.refreshCache();
        return Result.success();
    }

    // ==================== DictType Management ====================

    /**
     * 获取字典类型列表（管理用）
     * @return 字典类型列表
     */
    @GetMapping("/types")
    public Result<List<DictType>> getAllTypesForManagement() {
        return Result.success(dictService.getAllTypes());
    }

    /**
     * 根据ID获取字典类型
     * @param id 字典类型ID
     * @return 字典类型信息
     */
    @GetMapping("/types/{id}")
    public Result<DictType> getTypeById(@PathVariable Long id) {
        DictType type = dictService.getTypeById(id);
        if (type == null) {
            return Result.success(null);
        }
        return Result.success(type);
    }

    /**
     * 创建字典类型
     * @param dictType 字典类型信息
     * @return 创建的字典类型
     */
    @PostMapping("/types")
    public Result<DictType> createType(@RequestBody DictType dictType) {
        return Result.success(dictService.createType(dictType));
    }

    /**
     * 更新字典类型
     * @param id 字典类型ID
     * @param dictType 更新后的字典类型信息
     * @return 成功返回空结果
     */
    @PutMapping("/types/{id}")
    public Result<Void> updateType(@PathVariable Long id, @RequestBody DictType dictType) {
        dictType.setId(id);
        dictService.updateType(dictType);
        return Result.success();
    }

    /**
     * 删除字典类型
     * @param id 字典类型ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/types/{id}")
    public Result<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return Result.success();
    }

    // ==================== DictCode Management ====================

    /**
     * 获取字典项列表
     * @param typeId 可选，按类型筛选
     * @return 字典项列表
     */
    @GetMapping("/items")
    public Result<List<DictCode>> getAllItems(@RequestParam(required = false) Long typeId) {
        if (typeId != null) {
            return Result.success(dictService.getItemsByTypeId(typeId));
        }
        return Result.success(dictService.getAllItems());
    }

    /**
     * 根据ID获取字典项
     * @param id 字典项ID
     * @return 字典项信息
     */
    @GetMapping("/items/{id}")
    public Result<DictCode> getItemById(@PathVariable Long id) {
        DictCode item = dictService.getItemById(id);
        if (item == null) {
            return Result.success(null);
        }
        return Result.success(item);
    }

    /**
     * 创建字典项
     * @param dictCode 字典项信息
     * @return 创建的字典项
     */
    @PostMapping("/items")
    public Result<DictCode> createItem(@RequestBody DictCode dictCode) {
        return Result.success(dictService.createItem(dictCode));
    }

    /**
     * 更新字典项
     * @param id 字典项ID
     * @param dictCode 更新后的字典项信息
     * @return 成功返回空结果
     */
    @PutMapping("/items/{id}")
    public Result<Void> updateItem(@PathVariable Long id, @RequestBody DictCode dictCode) {
        dictCode.setId(id);
        dictService.updateItem(dictCode);
        return Result.success();
    }

    /**
     * 删除字典项
     * @param id 字典项ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        dictService.deleteItem(id);
        return Result.success();
    }
}
