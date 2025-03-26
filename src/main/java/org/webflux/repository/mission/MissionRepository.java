package org.webflux.repository.mission;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mission.MissionEntity;
import org.webflux.service.dto.RoleUserDeptDTO;

import java.util.List;
import java.util.Optional;

public interface MissionRepository {
    MissionEntity saveOrUpdate(JdbcTemplate jdbcTemplate, MissionEntity entity);
    MissionEntity findById(JdbcTemplate jdbcTemplate, Long id);
    MissionEntity findByCode(JdbcTemplate jdbcTemplate, String code);
    Optional<RoleUserDeptDTO> findByIdentifyCode(JdbcTemplate jdbcTemplate, String code);
    List<Long> findAllReceiverUserId(JdbcTemplate jdbcTemplate, String code);
    Optional<String> findDeptPath(JdbcTemplate jdbcTemplate, String code);
}
