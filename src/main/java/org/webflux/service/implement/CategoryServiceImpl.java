package org.webflux.service.implement;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.webflux.domain.Category;
import org.webflux.model.CategoryResponse;
import org.webflux.repository.CategoryRepository;
import org.webflux.repository.DatabaseConfigRepository;
import org.webflux.repository.query.DatabaseConfigQuery;
import org.webflux.service.CategoryService;
import org.webflux.service.DatabaseConfigService;
import org.webflux.service.dto.CategoryDTO;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Builder
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    @Override
    public List<Category> findAll() {
        return repository.findAll();
    }

    @Override
    public CategoryResponse findAll(int page, int pageSize, String type) {
        return repository.findAll(page, pageSize, type);
    }

    @Override
    public CategoryResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize, String type) {
        return repository.findAllOrderBy(sortColumn, sortType, page, pageSize, type);
    }

    @Override
    public Optional<Category> findById(int id) {
        return repository.findById(id);
    }

    @Override
    public CategoryResponse search(String type, String name, String content, String description, String createDateFrom, String createDateTo) {
        return repository.search(type, name, content, description, createDateFrom, createDateTo);
    }

    @Override
    public void update(CategoryDTO category) {
        repository.update(category);
    }

    @Override
    public void delete(Long id) {
        repository.delete(id);
    }

    @Override
    public void deleteMany(String ids) {
        repository.deleteMany(ids);
    }

    @Override
    public void saveCategory(CategoryDTO categoryDTO) {
        repository.saveCategory(categoryDTO);
    }

    @Override
    public List<Category> findAllByName(List<String> lstName) {
        return repository.findAllByName(lstName);
    }

    @Override
    public List<Category> findAllByType(List<String> lstType) {
        return repository.findAllByType(lstType);
    }

    @Override
    public Category findById(Long id) {
        return repository.findById(id);
    }
}