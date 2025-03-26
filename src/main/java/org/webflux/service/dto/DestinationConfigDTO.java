package org.webflux.service.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DestinationConfigDTO {
    private Long id;
    private String name;
}