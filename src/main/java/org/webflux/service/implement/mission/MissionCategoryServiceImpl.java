package org.webflux.service.implement.mission;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.webflux.domain.mission.CategoryEntity;
import org.webflux.repository.CategoryRepository;
import org.webflux.repository.mission.MissionCategoryRepository;
import org.webflux.service.mission.MissionCategoryService;

@Service
public class MissionCategoryServiceImpl implements MissionCategoryService {
    private MissionCategoryRepository categoryRepository;

    public MissionCategoryServiceImpl(MissionCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryEntity exist(JdbcTemplate jdbcTemplate) {
        return categoryRepository.exist(jdbcTemplate);
    }

    @Override
    public CategoryEntity insert(JdbcTemplate jdbcTemplate, CategoryEntity entity) {
        return categoryRepository.insert(jdbcTemplate, entity);
    }
}
