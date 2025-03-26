package org.webflux.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.webflux.service.dto.SyncOperatorDTO;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncFlowStatic {
    private Long id;
    private String name;
    private String description;
    private Long sourceId;
    private Long destinationId;
    private Boolean isActivated;
    private String updatedAt;
    private Long updatedBy;
    private String createdAt;
    private Long createdBy;
    private String proxy;
    private String payload;
    private String saveFilePath;
    private Boolean isGetSyncedAgain;
}