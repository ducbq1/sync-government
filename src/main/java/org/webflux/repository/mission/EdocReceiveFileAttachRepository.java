package org.webflux.repository.mission;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mission.EdocReceiveFileAttachEntity;

import java.util.List;

public interface EdocReceiveFileAttachRepository {
    EdocReceiveFileAttachEntity saveOrUpdate(JdbcTemplate jdbcTemplate, EdocReceiveFileAttachEntity entity);
    List<EdocReceiveFileAttachEntity> findByIsSync(JdbcTemplate jdbcTemplate, Long isSync, Long maxRow);
    EdocReceiveFileAttachEntity findByFileIdAndMissionId(JdbcTemplate jdbcTemplate, Long fileId, Long missionId);
}
