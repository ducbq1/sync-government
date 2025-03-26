package org.webflux.repository;

import org.webflux.domain.SyncFlowStatic;
import org.webflux.model.StaticFlowResponse;
import org.webflux.repository.query.SyncFlowStaticQuery;

import java.util.List;
import java.util.Optional;

public interface StaticFlowRepository {
    List<SyncFlowStaticQuery> findAll();
    StaticFlowResponse findAll(int page, int pageSize);

    StaticFlowResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize);

    Optional<SyncFlowStatic> findById(int id);

    StaticFlowResponse search(String name, String description, String sourceName, String destinationName);

    int updateOne(SyncFlowStatic syncFlowStatic);

    void updateMany(String flowId, Boolean status);

    void saveFlow(SyncFlowStatic syncFlowStatic);
}