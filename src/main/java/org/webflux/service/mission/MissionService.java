package org.webflux.service.mission;

import org.springframework.stereotype.Service;
import org.webflux.model.dto.ChiTietNhiemVuGiaoResponseDTO;
import org.webflux.model.dto.NhiemVuGiaoResponseDTO;

import javax.sql.DataSource;

@Service
public interface MissionService {
    void saveMission(DataSource dataSource, NhiemVuGiaoResponseDTO.NhiemVu nhiemVu, ChiTietNhiemVuGiaoResponseDTO.Item chiTietNhiemVu, String identifyCode, Boolean syncedAgain, String url);
}
