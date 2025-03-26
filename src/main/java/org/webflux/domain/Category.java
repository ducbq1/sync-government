package org.webflux.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {
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
    private String payload;
    private String token;
    private String sequence;
    private String uniqueKey;
    private String primaryKey;
}
