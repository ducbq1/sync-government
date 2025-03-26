package org.webflux.repository;

import org.webflux.domain.Category;
import org.webflux.repository.query.DatabaseConfigQuery;
import org.webflux.service.dto.CategoryDTO;
import org.webflux.model.CategoryResponse;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    List<Category> findAll();
    CategoryResponse findAll(int page, int pageSize, String type);

    CategoryResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize, String type);

    Optional<Category> findById(int id);

    CategoryResponse search(String type, String name, String content, String description, String createDateFrom, String createDateTo);

    void update(CategoryDTO category);

    void delete(Long id);

    void deleteMany(String ids);

    void saveCategory(CategoryDTO categoryDTO);
    List<Category> findAllByName(List<String> lstName);
    List<Category> findAllByType(List<String> lstType);
    Category findById(Long id);
}
