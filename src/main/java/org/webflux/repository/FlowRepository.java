package org.webflux.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Repository;
import org.webflux.domain.SyncFlow;
import org.webflux.model.flow.MainFlow;
import org.webflux.service.dto.MapStructureDTO;
import org.webflux.service.dto.MapStructureDetailDTO;
import org.webflux.service.dto.SyncOperatorDTO;

import java.util.List;

@Repository
public interface FlowRepository {
    Long saveDataFlow(String dataFlow);
    List<SyncFlow> findAll();
    List<MapStructureDTO> findAllMapStructure(Long syncFlowId);
    List<MapStructureDetailDTO> findAllMapStructureDetail(String mapStructureId);
    List<SyncOperatorDTO> findAllSyncOperator(Long syncFlowId);
    Long saveMapStruct(MapStructureDTO mapStructureDTO);
    Long saveMapDetail(MapStructureDetailDTO mapStructureDetailDTO);
    Long saveSyncOperator(SyncOperatorDTO syncOperatorDTO);
    void deleteAllDataFlow();
    void deleteAllMapStruct();
    void deleteAllMapDetail();
    void deleteAllSyncOperator();
}
