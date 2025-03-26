package org.webflux.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleUserDeptDTO {
    private Long userId;
    private Long roleId;
    private Long deptId;
}
