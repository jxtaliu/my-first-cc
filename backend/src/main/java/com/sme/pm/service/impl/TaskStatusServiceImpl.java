package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sme.pm.entity.DictCode;
import com.sme.pm.entity.TaskStatus;
import com.sme.pm.mapper.DictCodeMapper;
import com.sme.pm.mapper.TaskStatusMapper;
import com.sme.pm.service.ITaskStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusServiceImpl extends ServiceImpl<TaskStatusMapper, TaskStatus> implements ITaskStatusService {

    private static final Logger log = LoggerFactory.getLogger(TaskStatusServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DictCodeMapper dictCodeMapper;

    @Override
    public List<TaskStatus> findByProjectId(String projectId) {
        LambdaQueryWrapper<TaskStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskStatus::getProjectId, projectId)
               .eq(TaskStatus::getDeleted, 0)
               .orderByAsc(TaskStatus::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<TaskStatus> findSystemDefaults() {
        LambdaQueryWrapper<TaskStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(TaskStatus::getProjectId)
               .eq(TaskStatus::getDeleted, 0)
               .orderByAsc(TaskStatus::getSortOrder);
        return list(wrapper);
    }

    @Override
    public TaskStatus findByCode(String projectId, String code) {
        LambdaQueryWrapper<TaskStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskStatus::getProjectId, projectId)
               .eq(TaskStatus::getCode, code)
               .eq(TaskStatus::getDeleted, 0);
        return getOne(wrapper);
    }

    @Override
    public void initializeFromDict(String projectId) {
        // Check if already initialized
        List<TaskStatus> existing = findByProjectId(projectId);
        if (existing != null && !existing.isEmpty()) {
            return; // Already initialized
        }

        List<DictCode> dictCodes = dictCodeMapper.findByTypeCode("task_status");
        for (DictCode dictCode : dictCodes) {
            TaskStatus taskStatus = new TaskStatus();
            taskStatus.setProjectId(projectId);
            taskStatus.setCode(dictCode.getCode());
            taskStatus.setName(dictCode.getName());
            taskStatus.setNameEn(dictCode.getNameEn());
            taskStatus.setNameZh(dictCode.getNameZh());
            taskStatus.setSortOrder(dictCode.getSortOrder());
            taskStatus.setCategory(getCategoryByCode(dictCode.getCode()));

            // Extract color from extra JSON field using Jackson
            if (dictCode.getExtra() != null) {
                try {
                    JsonNode extraNode = objectMapper.readTree(dictCode.getExtra());
                    JsonNode colorNode = extraNode.get("color");
                    if (colorNode != null) {
                        taskStatus.setColor(colorNode.asText());
                    }
                } catch (Exception e) {
                    log.debug("Failed to parse color from extra JSON for dict code: {}", dictCode.getCode(), e);
                }
            }
            save(taskStatus);
        }
    }

    private String getCategoryByCode(String code) {
        if (code == null) return "todo";
        String upper = code.toUpperCase();
        if (upper.contains("DONE") || upper.contains("COMPLETED")) return "done";
        if (upper.contains("REVIEW") || upper.contains("TEST")) return "alert";
        if (upper.contains("PROGRESS") || upper.contains("DEVELOPMENT")) return "doing";
        return "todo";
    }
}
