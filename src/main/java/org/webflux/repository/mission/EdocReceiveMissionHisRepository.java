package org.webflux.repository.mission;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mission.EdocReceiveMissionHisEntity;

public interface EdocReceiveMissionHisRepository {
    EdocReceiveMissionHisEntity findFirstByHistoryId(JdbcTemplate jdbcTemplate, Long historyId);
    void saveOrUpdate(JdbcTemplate jdbcTemplate, EdocReceiveMissionHisEntity entity);
}
