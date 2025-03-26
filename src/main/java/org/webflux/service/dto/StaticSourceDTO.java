package org.webflux.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"id", "name", "type", "content", "description", "isActivated", "editButton"})
@JsonIgnoreProperties({"createdBy", "createdAt", "updatedBy", "updatedAt"})
public class StaticSourceDTO {
    private Long id;
    private String name;
    private String description;
    private String content;
    private String type;
    private Boolean isActivated;
    private Long createdBy;
    private String createdAt;
    private Long updatedBy;
    private String updatedAt;
    private StaticFlowDTO.Button editButton;

    @Data
    @Builder
    public static class Button {
        private String icon;
        private String callback;
    }
}
