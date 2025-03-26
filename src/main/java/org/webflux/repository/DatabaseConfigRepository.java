package org.webflux.repository;

import org.webflux.domain.Category;
import org.webflux.model.CategoryResponse;
import org.webflux.repository.query.DatabaseConfigQuery;
import org.webflux.service.dto.CategoryDTO;

import java.util.List;
import java.util.Optional;

public interface DatabaseConfigRepository {
    Optional<DatabaseConfigQuery> findById(Long id);
    Optional<List<DatabaseConfigQuery>> findAll();
    void optimizeDatabase();
}
