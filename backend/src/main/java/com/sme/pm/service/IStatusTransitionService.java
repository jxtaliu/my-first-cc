package com.sme.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sme.pm.entity.StatusTransition;

import java.util.List;

public interface IStatusTransitionService extends IService<StatusTransition> {
    List<StatusTransition> findByProjectId(Long projectId);
    List<StatusTransition> findByFromStatus(Long fromStatusId);
    boolean existsTransition(Long fromStatusId, Long toStatusId);
}
