package org.webflux.repository.mission;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mission.EdocMissionCombineEntity;

public interface EdocMissionCombineRepository {
    EdocMissionCombineEntity findFirstByEdocReceiveMissionIdCodeCombineCode(JdbcTemplate jdbcTemplate, Long edocReceiveMissionId, String code, String combineCode, String combineName);
    void saveOrUpdate(JdbcTemplate jdbcTemplate, EdocMissionCombineEntity entity);
}
