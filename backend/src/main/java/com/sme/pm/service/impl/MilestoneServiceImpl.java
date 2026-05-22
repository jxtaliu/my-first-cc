package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        LambdaQueryWrapper<Milestone> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Milestone::getDeleted, 0)
               .orderByAsc(Milestone::getTargetDate);
        return list(wrapper);
    }

    @Override
    public List<Milestone> findCrossProject() {
        LambdaQueryWrapper<Milestone> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Milestone::getIsCrossProject, true)
               .eq(Milestone::getDeleted, 0)
               .orderByAsc(Milestone::getTargetDate);
        return list(wrapper);
    }

    @Override
    public List<Milestone> findByProjectId(String projectId) {
        LambdaQueryWrapper<Milestone> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Milestone::getProjectId, projectId)
               .eq(Milestone::getDeleted, 0)
               .orderByAsc(Milestone::getTargetDate);
        return list(wrapper);
    }

    @Override
    public List<Milestone> findDueSoon(int days) {
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(days);
        LambdaQueryWrapper<Milestone> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Milestone::getTargetDate, today)
               .le(Milestone::getTargetDate, deadline)
               .eq(Milestone::getDeleted, 0)
               .orderByAsc(Milestone::getTargetDate);
        return list(wrapper);
    }
}
