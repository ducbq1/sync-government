package org.webflux.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MapStructureDTO {
    private Long id;
    private SyncOperatorDTO source;
    private SyncOperatorDTO destination;
    private Long syncFlowId;
    private List<MapStructureDetailDTO> listDetail;
}
