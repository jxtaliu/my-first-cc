package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.BugStatus;
import com.sme.pm.entity.BugStatusTransition;
import com.sme.pm.mapper.BugStatusMapper;
import com.sme.pm.mapper.BugStatusTransitionMapper;
import com.sme.pm.service.IBugStatusService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BugStatusServiceImpl extends ServiceImpl<BugStatusMapper, BugStatus> implements IBugStatusService {

    // Default bug statuses
    private static final List<BugStatus> DEFAULT_STATUSES = List.of(
            createStatus("OPEN", "Open", "待办", "#EF4444", 1),
            createStatus("IN_PROGRESS", "In Progress", "修复中", "#F59E0B", 2),
            createStatus("IN_TEST", "In Test", "待验证", "#3B82F6", 3),
            createStatus("CLOSED", "Closed", "已关闭", "#10B981", 4),
            createStatus("REOPENED", "Reopened", "重新打开", "#8B5CF6", 5)
    );

    // Default transitions
    private static final List<String[]> DEFAULT_TRANSITIONS = List.of(
            new String[]{"OPEN", "IN_PROGRESS"},
            new String[]{"OPEN", "CLOSED"},
            new String[]{"IN_PROGRESS", "IN_TEST"},
            new String[]{"IN_PROGRESS", "REOPENED"},
            new String[]{"IN_TEST", "CLOSED"},
            new String[]{"IN_TEST", "IN_PROGRESS"},
            new String[]{"CLOSED", "REOPENED"},
            new String[]{"REOPENED", "IN_PROGRESS"}
    );

    private final BugStatusTransitionMapper transitionMapper;

    public BugStatusServiceImpl(BugStatusTransitionMapper transitionMapper) {
        this.transitionMapper = transitionMapper;
    }

    private static BugStatus createStatus(String code, String nameEn, String nameZh, String color, int sortOrder) {
        BugStatus status = new BugStatus();
        status.setCode(code);
        status.setNameEn(nameEn);
        status.setNameZh(nameZh);
        status.setColor(color);
        status.setSortOrder(sortOrder);
        return status;
    }

    @Override
    public List<BugStatus> getByProjectId(String projectId) {
        // First try project-specific statuses
        LambdaQueryWrapper<BugStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BugStatus::getProjectId, projectId)
                .eq(BugStatus::getDeleted, 0)
                .orderByAsc(BugStatus::getSortOrder);
        List<BugStatus> projectStatuses = list(wrapper);

        if (projectStatuses != null && !projectStatuses.isEmpty()) {
            return projectStatuses;
        }

        // Fallback to system default
        return getDefaultStatuses();
    }

    @Override
    public List<BugStatus> getDefaultStatuses() {
        LambdaQueryWrapper<BugStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(BugStatus::getProjectId)
                .eq(BugStatus::getDeleted, 0)
                .orderByAsc(BugStatus::getSortOrder);
        List<BugStatus> defaultStatuses = list(wrapper);

        if (defaultStatuses == null || defaultStatuses.isEmpty()) {
            // Return hardcoded defaults if no records in DB
            return new ArrayList<>(DEFAULT_STATUSES);
        }
        return defaultStatuses;
    }

    @Override
    public boolean canTransition(String projectId, String fromStatus, String toStatus) {
        // First check project-specific transitions
        LambdaQueryWrapper<BugStatusTransition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BugStatusTransition::getProjectId, projectId)
                .eq(BugStatusTransition::getFromStatus, fromStatus)
                .eq(BugStatusTransition::getToStatus, toStatus)
                .eq(BugStatusTransition::getDeleted, 0);
        boolean canTransition = transitionMapper.exists(wrapper);

        if (canTransition) {
            return true;
        }

        // Fallback to system default transitions
        LambdaQueryWrapper<BugStatusTransition> defaultWrapper = new LambdaQueryWrapper<>();
        defaultWrapper.isNull(BugStatusTransition::getProjectId)
                .eq(BugStatusTransition::getFromStatus, fromStatus)
                .eq(BugStatusTransition::getToStatus, toStatus)
                .eq(BugStatusTransition::getDeleted, 0);
        return transitionMapper.exists(defaultWrapper);
    }

    @Override
    public List<String> getAllowedTransitions(String projectId, String fromStatus) {
        List<String> transitions = new ArrayList<>();

        // Get project-specific transitions
        LambdaQueryWrapper<BugStatusTransition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BugStatusTransition::getProjectId, projectId)
                .eq(BugStatusTransition::getFromStatus, fromStatus)
                .eq(BugStatusTransition::getDeleted, 0);
        List<BugStatusTransition> projectTransitions = transitionMapper.selectList(wrapper);

        if (projectTransitions != null && !projectTransitions.isEmpty()) {
            transitions.addAll(projectTransitions.stream()
                    .map(BugStatusTransition::getToStatus)
                    .collect(Collectors.toList()));
        } else {
            // Fallback to default transitions
            for (String[] transition : DEFAULT_TRANSITIONS) {
                if (transition[0].equals(fromStatus)) {
                    transitions.add(transition[1]);
                }
            }
        }

        return transitions;
    }

    @Override
    public void initializeForProject(String projectId) {
        // Check if already initialized
        LambdaQueryWrapper<BugStatus> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(BugStatus::getProjectId, projectId);
        if (count(checkWrapper) > 0) {
            return; // Already initialized
        }

        // Insert default statuses
        for (BugStatus status : DEFAULT_STATUSES) {
            BugStatus newStatus = new BugStatus();
            newStatus.setProjectId(projectId);
            newStatus.setCode(status.getCode());
            newStatus.setNameEn(status.getNameEn());
            newStatus.setNameZh(status.getNameZh());
            newStatus.setColor(status.getColor());
            newStatus.setSortOrder(status.getSortOrder());
            save(newStatus);
        }

        // Insert default transitions
        for (String[] transition : DEFAULT_TRANSITIONS) {
            BugStatusTransition newTransition = new BugStatusTransition();
            newTransition.setProjectId(projectId);
            newTransition.setFromStatus(transition[0]);
            newTransition.setToStatus(transition[1]);
            transitionMapper.insert(newTransition);
        }
    }
}
