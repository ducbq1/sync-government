package org.webflux.service.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SourceConfigDTO {
    private Long id;
    private String name;
}