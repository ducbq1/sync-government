package org.webflux.repository.mission;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mission.AttachsEntity;

public interface AttachsRepository {
    AttachsEntity saveOrUpdate(JdbcTemplate jdbcTemplate, AttachsEntity entity);
    AttachsEntity findByFileIdAndObjectId(JdbcTemplate jdbcTemplate, Long fileId, Long objectId);
}
