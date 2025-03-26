package org.webflux.repository;

import org.webflux.domain.DatabaseConfig;
import org.webflux.model.StaticDestinationResponse;

import java.util.Optional;

public interface DestinationRepository {
    StaticDestinationResponse findAll(int page, int pageSize);

    StaticDestinationResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize);

    Optional<DatabaseConfig> findById(int id);

    void updateOne(DatabaseConfig category);

    void updateMany(String configId, Boolean status);

    void updateConnection(int configID, long updatedBy, boolean isConnected);

    void saveEntity(DatabaseConfig databaseConfig);

    StaticDestinationResponse search(String name, String description, String url);
}