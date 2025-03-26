package org.webflux.repository;

import org.webflux.domain.Category;
import org.webflux.model.StaticSourceResponse;

import java.util.Optional;

public interface SourceRepository {
    StaticSourceResponse findAll(int page, int pageSize);

    StaticSourceResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize);

    Optional<Category> findById(int id);

    void updateOne(Category category);

    void updateMany(String sourceId, Boolean status);

    void saveEntity(Category category);

    StaticSourceResponse search(String name, String description, String content);
}