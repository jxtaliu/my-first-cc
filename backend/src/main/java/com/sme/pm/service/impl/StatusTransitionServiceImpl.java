package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.StatusTransition;
import com.sme.pm.mapper.StatusTransitionMapper;
import com.sme.pm.service.IStatusTransitionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusTransitionServiceImpl extends ServiceImpl<StatusTransitionMapper, StatusTransition> implements IStatusTransitionService {

    @Override
    public List<StatusTransition> findByProjectId(Long projectId) {
        LambdaQueryWrapper<StatusTransition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StatusTransition::getProjectId, projectId)
               .eq(StatusTransition::getDeleted, 0);
        return list(wrapper);
    }

    @Override
    public List<StatusTransition> findByFromStatus(Long fromStatusId) {
        LambdaQueryWrapper<StatusTransition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StatusTransition::getFromStatusId, fromStatusId)
               .eq(StatusTransition::getDeleted, 0);
        return list(wrapper);
    }

    @Override
    public boolean existsTransition(Long fromStatusId, Long toStatusId) {
        LambdaQueryWrapper<StatusTransition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StatusTransition::getFromStatusId, fromStatusId)
               .eq(StatusTransition::getToStatusId, toStatusId)
               .eq(StatusTransition::getDeleted, 0);
        return count(wrapper) > 0;
    }
}
