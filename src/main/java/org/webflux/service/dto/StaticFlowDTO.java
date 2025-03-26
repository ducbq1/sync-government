package org.webflux.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"id", "name", "description", "sourceName", "destinationName", "isActivated", "editButton"})
@JsonIgnoreProperties({"sourceId", "destinationId", "createdBy", "createdAt", "updatedBy", "updatedAt"})
public class StaticFlowDTO {
    private Long id;
    private String name;
    private String description;
    private Long sourceId;
    private Long destinationId;
    private String sourceName;
    private String destinationName;
    private Boolean isActivated;
    private Long createdBy;
    private String createdAt;
    private Long updatedBy;
    private String updatedAt;
    private String proxy;
    private String payload;
    private String saveFilePath;
    private Button editButton;

    @Data
    @Builder
    public static class Button {
        private String icon;
        private String callback;
    }
}