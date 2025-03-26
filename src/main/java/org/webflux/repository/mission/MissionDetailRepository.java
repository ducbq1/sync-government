package org.webflux.repository.mission;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mission.MissionDetailEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface MissionDetailRepository {
    MissionDetailEntity saveOrUpdate(JdbcTemplate jdbcTemplate, MissionDetailEntity entity);
    MissionDetailEntity findById(JdbcTemplate jdbcTemplate, Long id);
    MissionDetailEntity findByMissionId(JdbcTemplate jdbcTemplate, Long missionId);
    List<MissionDetailEntity> findAll(JdbcTemplate jdbcTemplate);
    List<MissionDetailEntity> findByCondition(JdbcTemplate jdbcTemplate, Predicate<MissionDetailEntity> condition);
    List<MissionDetailEntity> findByCondition(JdbcTemplate jdbcTemplate, Map<String, Object> ...condition);
}
