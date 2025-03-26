package org.webflux.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import org.webflux.domain.SyncFlow;
import org.webflux.domain.SyncFlowStatic;
import org.webflux.model.StaticFlowResponse;
import org.webflux.repository.query.SyncFlowStaticQuery;
import org.webflux.service.dto.MapStructureDTO;
import org.webflux.service.dto.MapStructureDetailDTO;
import org.webflux.service.dto.SyncOperatorDTO;

import java.util.List;
import java.util.Optional;

@Service
public interface StaticFlowService {
    List<SyncFlowStaticQuery> findAll();
    StaticFlowResponse findAll(int page, int pageSize);

    StaticFlowResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize);

    Optional<SyncFlowStatic> findById(int id);

    StaticFlowResponse search(String name, String description, String sourceName, String destinationName);

    Optional<Integer> updateOne(SyncFlowStatic syncFlowStatic) throws JsonProcessingException;

    void updateMany(String flowId, Boolean status);

    void saveFlow(SyncFlowStatic syncFlowStatic) throws JsonProcessingException;
}
