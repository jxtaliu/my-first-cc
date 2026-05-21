package com.sme.pm.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoleAssignRequest {
    private List<Long> roleIds;
}
