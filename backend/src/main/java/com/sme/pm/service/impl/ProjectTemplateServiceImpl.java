package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.ProjectTemplate;
import com.sme.pm.mapper.ProjectTemplateMapper;
import com.sme.pm.service.IProjectTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectTemplateServiceImpl extends ServiceImpl<ProjectTemplateMapper, ProjectTemplate> implements IProjectTemplateService {

    @Override
    public List<ProjectTemplate> findAll() {
        LambdaQueryWrapper<ProjectTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectTemplate::getDeleted, 0)
               .orderByDesc(ProjectTemplate::getCreatedAt);
        return list(wrapper);
    }

    @Override
    public ProjectTemplate findById(Long id) {
        LambdaQueryWrapper<ProjectTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectTemplate::getId, id)
               .eq(ProjectTemplate::getDeleted, 0);
        return getOne(wrapper);
    }
}
