package org.webflux.repository.mission;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mission.EdocReceiveMissionEntity;

import java.util.List;

public interface EdocReceiveMissionRepository {
    EdocReceiveMissionEntity findFirstByCode(JdbcTemplate jdbcTemplate, String code);
    void saveOrUpdate(JdbcTemplate jdbcTemplate, EdocReceiveMissionEntity entity);
    List<EdocReceiveMissionEntity> findAllByStatusSys(JdbcTemplate jdbcTemplate, Long statusSys);
}
