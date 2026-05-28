package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.Milestone;
import com.sme.pm.mapper.MilestoneMapper;
import com.sme.pm.service.IMilestoneService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MilestoneServiceImpl extends ServiceImpl<MilestoneMapper, Milestone> implements IMilestoneService {

    @Override
    public List<Milestone> findAll() {
        return baseMapper.findAll();
    }

    @Override
    public List<Milestone> findCrossProject() {
        return baseMapper.findCrossProject();
    }

    @Override
    public List<Milestone> findByProjectId(String projectId) {
        return baseMapper.findByProjectId(projectId);
    }

    @Override
    public List<Milestone> findDueSoon(int days) {
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(days);
        return baseMapper.findDueSoon(deadline);
    }

    @Override
    public void completeMilestone(Long id) {
        Milestone milestone = baseMapper.findById(id);
        if (milestone == null) {
            throw new IllegalArgumentException("Milestone not found");
        }
        milestone.setStatus("COMPLETED");
        baseMapper.updateById(milestone);
    }
}
