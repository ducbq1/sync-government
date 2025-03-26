package org.webflux.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"id", "actionLog", "content", "detail", "isError", "createdAt", "editButton", "deleteButton"})
@JsonIgnoreProperties({"createdBy"})
public class AuditLogDTO {
    private Long id;
    private Long createdBy;
    private String createdAt;
    private String actionLog;
    private String content;
    private String detail;
    private Boolean isError;
    private Button deleteButton;
    private Button editButton;

    @Data
    @Builder
    public static class Button {
        private String icon;
        private String callback;
    }
}

