package org.webflux.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import org.webflux.domain.SyncFlow;
import org.webflux.service.dto.MapStructureDTO;
import org.webflux.service.dto.MapStructureDetailDTO;
import org.webflux.service.dto.SyncOperatorDTO;

import java.util.List;

@Service
public interface FlowService {
    void save(String dataFlow) throws JsonProcessingException;
    List<MapStructureDTO> findAllMapStructure(Long syncFlowId);
    List<MapStructureDetailDTO> findAllMapStructureDetail(String mapStructureId);
    List<SyncOperatorDTO> findAllSyncOperator(Long syncFlowId);
    List<SyncFlow> findAll();
}
