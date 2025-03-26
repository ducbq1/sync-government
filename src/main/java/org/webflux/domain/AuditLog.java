package org.webflux.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
    private Long id;
    private Long createdBy;
    private String createdAt;
    private String action;
    private String content;
    private String detail;
    private Boolean isError;
}
