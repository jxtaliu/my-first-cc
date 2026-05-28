package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.DataStandard;
import com.sme.pm.service.IDataStandardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据标准控制器
 * 提供数据标准管理相关API，包括数据标准CRUD、共享等
 *
 * 用法：
 * - GET /api/v1/data-standards - 获取数据标准列表
 * - GET /api/v1/data-standards/{id} - 获取数据标准详情
 * - POST /api/v1/data-standards - 创建数据标准
 * - PUT /api/v1/data-standards/{id} - 更新数据标准
 * - DELETE /api/v1/data-standards/{id} - 删除数据标准
 * - POST /api/v1/data-standards/{id}/share - 共享数据标准
 */
@RestController
@RequestMapping("/api/v1/data-standards")
public class DataStandardController {

    private final IDataStandardService dataStandardService;

    public DataStandardController(IDataStandardService dataStandardService) {
        this.dataStandardService = dataStandardService;
    }

    /**
     * 获取数据标准列表
     * @param type 可选，按类型筛选
     * @return 数据标准列表
     */
    @GetMapping
    public Result<List<DataStandard>> list(@RequestParam(required = false) String type) {
        if (type != null && !type.isEmpty()) {
            return Result.success(dataStandardService.getByType(type));
        }
        return Result.success(dataStandardService.getAll());
    }

    /**
     * 获取数据标准详情
     * @param id 数据标准ID
     * @return 数据标准详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        return Result.success(dataStandardService.getDetail(id));
    }

    /**
     * 创建数据标准
     * @param dataStandard 数据标准信息
     * @return 创建的数据标准
     */
    @PostMapping
    public Result<DataStandard> create(@RequestBody DataStandard dataStandard) {
        return Result.success(dataStandardService.create(dataStandard));
    }

    /**
     * 更新数据标准
     * @param id 数据标准ID
     * @param dataStandard 更新后的数据标准信息
     * @return 更新后的数据标准
     */
    @PutMapping("/{id}")
    public Result<DataStandard> update(@PathVariable Long id, @RequestBody DataStandard dataStandard) {
        return Result.success(dataStandardService.update(id, dataStandard));
    }

    /**
     * 删除数据标准
     * @param id 数据标准ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dataStandardService.delete(id);
        return Result.success();
    }

    /**
     * 共享数据标准
     * @param id 数据标准ID
     * @param shareParams 共享参数
     * @return 成功返回空结果
     */
    @PostMapping("/{id}/share")
    public Result<Void> share(@PathVariable Long id, @RequestBody Map<String, Object> shareParams) {
        dataStandardService.share(id, shareParams);
        return Result.success();
    }
}
