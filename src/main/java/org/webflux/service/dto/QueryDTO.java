package org.webflux.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueryDTO {
    private Long databaseConfigId;
    private String query;
}
