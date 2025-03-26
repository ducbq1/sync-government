package org.webflux.service.mission;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.webflux.domain.mission.CategoryEntity;

@Service
public interface MissionCategoryService {
    CategoryEntity exist(JdbcTemplate jdbcTemplate);
    CategoryEntity insert(JdbcTemplate jdbcTemplate, CategoryEntity entity);
}
