package org.webflux.repository.mission;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mission.MissionHisEntity;
import org.webflux.model.dto.DieuChinhThoiGianNhiemVuDTO;
import org.webflux.model.dto.GuiBaoCaoNhiemVuRequestDTO;
import org.webflux.model.dto.TienDoXuLyNhiemVuRequestDTO;
import org.webflux.model.dto.TraLaiNhiemVuRequestDTO;

import java.util.List;

public interface MissionHisRepository {
    MissionHisEntity saveOrUpdate(JdbcTemplate jdbcTemplate, MissionHisEntity entity);
    MissionHisEntity findById(JdbcTemplate jdbcTemplate, Long id);
    List<String> getStringTraLaiNhiemVu(JdbcTemplate jdbcTemplate);
    List<TraLaiNhiemVuRequestDTO> getBodyTraLaiNhiemVu(JdbcTemplate jdbcTemplate);
    List<GuiBaoCaoNhiemVuRequestDTO> getBodyGuiBaoCao(JdbcTemplate jdbcTemplat);
    List<TienDoXuLyNhiemVuRequestDTO> getBodyTienDoXuLy(JdbcTemplate jdbcTemplate, String basePath);
    List<DieuChinhThoiGianNhiemVuDTO> getBodyDieuChinhThoiGianNhiemVu(JdbcTemplate jdbcTemplate, String basePath);
}
