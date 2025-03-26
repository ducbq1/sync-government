package org.webflux.service.mission;

import org.springframework.stereotype.Service;
import org.webflux.domain.mission.EdocReceiveMissionEntity;
import org.webflux.model.dto.ChiTietNhiemVuGiaoResponseDTO;

import javax.sql.DataSource;

@Service
public interface EdocMissionCombineService {
    void saveEdocMissionCombine(DataSource dataSource, ChiTietNhiemVuGiaoResponseDTO.Item nhiemVuChiTiet, EdocReceiveMissionEntity mission, String maDonViDuocGiao);
}
