package org.webflux.repository.mission;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mission.CategoryEntity;

public interface MissionCategoryRepository {
    CategoryEntity exist(JdbcTemplate jdbcTemplate);
    CategoryEntity insert(JdbcTemplate jdbcTemplate, CategoryEntity entity);
}
