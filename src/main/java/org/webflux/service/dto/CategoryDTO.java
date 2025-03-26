package org.webflux.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"id", "name", "type", "content", "description", "isDeleted", "isActivated", "editButton", "deleteButton"})
@JsonIgnoreProperties({"createdBy", "updatedBy", "createdAt", "updatedAt", "databaseConfigId"})
public class CategoryDTO {
    private Long id;
    private Long createdBy;
    private String createdAt;
    private Long updatedBy;
    private String updatedAt;
    private Boolean isDeleted;
    private Boolean isActivated;
    private String name;
    private String type;
    private String content;
    private String description;
    private Long databaseConfigId;
    private Button deleteButton;
    private Button editButton;

    @Data
    @Builder
    public static class Button {
        private String icon;
        private String callback;
    }
}

