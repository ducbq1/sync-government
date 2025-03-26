package org.webflux.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MapStructureDetailDTO {
    private Long id;
    private String sourceField;
    private String destinationField;
    private Long mapStructureId;
}
