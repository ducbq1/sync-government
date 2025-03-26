package org.webflux.service.mission;

import org.springframework.stereotype.Service;
import org.webflux.domain.mission.EdocReceiveMissionEntity;
import org.webflux.model.dto.ChiTietNhiemVuGiaoResponseDTO;
import org.webflux.model.dto.NhiemVuGiaoResponseDTO;

import javax.sql.DataSource;
import java.util.List;

@Service
public interface EdocReceiveMissionService {
    void saveEdocReceiveMission(DataSource dataSource, NhiemVuGiaoResponseDTO.NhiemVu nhiemVu);
    void saveEdocReceiveMissionDetail(DataSource dataSource, ChiTietNhiemVuGiaoResponseDTO.Item nhiemVuChiTiet, String maDonViDuocGiao);
    List<EdocReceiveMissionEntity> getAllMissionByStatusSys(DataSource dataSource, Long statusSys);
}
