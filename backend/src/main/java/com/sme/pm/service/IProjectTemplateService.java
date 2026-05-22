package com.sme.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sme.pm.entity.ProjectTemplate;

import java.util.List;

public interface IProjectTemplateService extends IService<ProjectTemplate> {
    List<ProjectTemplate> findAll();
    ProjectTemplate findById(Long id);
}
