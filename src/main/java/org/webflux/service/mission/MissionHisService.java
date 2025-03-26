package org.webflux.service.mission;

import org.springframework.stereotype.Service;
import org.webflux.domain.mission.MissionDetailEntity;
import org.webflux.domain.mission.MissionEntity;
import org.webflux.domain.mission.MissionHisEntity;
import org.webflux.model.dto.*;

import javax.sql.DataSource;
import java.util.List;

@Service
public interface MissionHisService {
    void saveMissionHis(DataSource dataSource, MissionEntity mission, MissionDetailEntity missionDetail, ChiTietNhiemVuGiaoResponseDTO.Item chiTietNhiemVu, Long userId, String identifyCode);
    MissionHisEntity saveOrUpdate(DataSource dataSource, MissionHisEntity entity);
    MissionHisEntity findById(DataSource dataSource, Long id);
    List<String> getStringTraLaiNhiemVu(DataSource dataSource);
    List<TraLaiNhiemVuRequestDTO> getBodyTraLaiNhiemVu(DataSource dataSource);
    List<GuiBaoCaoNhiemVuRequestDTO> getBodyGuiBaoCao(DataSource dataSource);
    List<TienDoXuLyNhiemVuRequestDTO> getBodyTienDoXuLy(DataSource dataSource, String basePath);
    List<DieuChinhThoiGianNhiemVuDTO> getBodyDieuChinhThoiGianNhiemVu(DataSource dataSource, String basePath);

}
