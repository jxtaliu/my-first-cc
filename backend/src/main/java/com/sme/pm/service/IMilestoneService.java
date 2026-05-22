package com.sme.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sme.pm.entity.Milestone;

import java.util.List;

public interface IMilestoneService extends IService<Milestone> {
    List<Milestone> findAll();
    List<Milestone> findCrossProject();
    List<Milestone> findByProjectId(String projectId);
    List<Milestone> findDueSoon(int days);
}
