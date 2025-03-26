package org.webflux.service.mission;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.webflux.domain.mission.MissionDetailEntity;
import org.webflux.domain.mission.MissionEntity;
import org.webflux.model.dto.ChiTietNhiemVuGiaoResponseDTO;
import org.webflux.model.dto.NhiemVuGiaoResponseDTO;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public interface MissionDetailService {
    MissionDetailEntity saveOrUpdate(JdbcTemplate jdbcTemplate, MissionDetailEntity entity);
    MissionDetailEntity saveMissionDetail(DataSource dataSource, MissionEntity mission, NhiemVuGiaoResponseDTO.NhiemVu nhiemVu, ChiTietNhiemVuGiaoResponseDTO.Item chiTietNhiemVu, Long deptId, List<Long> lstUserReceiveId);
    List<MissionDetailEntity> findAll(DataSource dataSource);
    List<MissionDetailEntity> findByCondition(DataSource dataSource, Predicate<MissionDetailEntity> condition);
    List<MissionDetailEntity> findByCondition(DataSource dataSource, Map<String, Object> ...condition);
}
