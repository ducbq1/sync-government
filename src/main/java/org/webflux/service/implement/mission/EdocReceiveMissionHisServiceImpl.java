package org.webflux.service.implement.mission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.webflux.domain.mission.EdocMissionCombineEntity;
import org.webflux.domain.mission.EdocReceiveMissionEntity;
import org.webflux.domain.mission.EdocReceiveMissionHisEntity;
import org.webflux.model.dto.ChiTietNhiemVuGiaoResponseDTO;
import org.webflux.helper.CommonUtils;
import org.webflux.helper.Constants;
import org.webflux.repository.mission.EdocReceiveMissionHisRepository;
import org.webflux.service.mission.EdocReceiveMissionHisService;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;

@Service
public class EdocReceiveMissionHisServiceImpl implements EdocReceiveMissionHisService {

    private EdocReceiveMissionHisRepository edocReceiveMissionHisRepository;

    public EdocReceiveMissionHisServiceImpl(EdocReceiveMissionHisRepository edocReceiveMissionHisRepository) {
        this.edocReceiveMissionHisRepository = edocReceiveMissionHisRepository;
    }

    @Override
//    @Transactional
    public void saveEdocReceiveMissionHis(DataSource dataSource, ChiTietNhiemVuGiaoResponseDTO.Item nhiemVuChiTiet, EdocReceiveMissionEntity mission) throws Exception {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    List<ChiTietNhiemVuGiaoResponseDTO.TienDoXuLy> lstTienDoXuLy = nhiemVuChiTiet.getTienDoXuLy();
                    if(!CollectionUtils.isEmpty(lstTienDoXuLy)) {
                        for (ChiTietNhiemVuGiaoResponseDTO.TienDoXuLy tienDo : lstTienDoXuLy) {
                            EdocReceiveMissionHisEntity entity = edocReceiveMissionHisRepository.findFirstByHistoryId(jdbcTemplate, CommonUtils.parseStringToLong(tienDo.getMaLichSuTienDoXuLy()));
                            entity = convertTienDoXuLyToEdocMissionHisEntity(entity, tienDo);
                            entity.setEdocReceiveMissionId(mission.getEdocReceiveMissionId());
                            entity.setCode(nhiemVuChiTiet.getMaNhiemVu());
                            entity.setToIdentifyCode(nhiemVuChiTiet.getMaDonViDuocGiao());
                            entity.setToDeptName(nhiemVuChiTiet.getTenCoQuanDuocGiao());
                            edocReceiveMissionHisRepository.saveOrUpdate(jdbcTemplate, entity);
                        }
                    }

                    List<ChiTietNhiemVuGiaoResponseDTO.LichSuGuiTraLai> lstLichSuGuiTra = nhiemVuChiTiet.getLichSuGuiTraLaiBoDiaPhuong();
                    if(!CollectionUtils.isEmpty(lstLichSuGuiTra)) {
                        for (ChiTietNhiemVuGiaoResponseDTO.LichSuGuiTraLai traLai : lstLichSuGuiTra) {
                            EdocReceiveMissionHisEntity entity = edocReceiveMissionHisRepository.findFirstByHistoryId(jdbcTemplate, CommonUtils.parseStringToLong(traLai.getMaLichSuGuiTra()));
                            entity = convertLichSuTraToEdocMissionHisEntity(entity, traLai);
                            entity.setEdocReceiveMissionId(mission.getEdocReceiveMissionId());
                            entity.setCode(nhiemVuChiTiet.getMaNhiemVu());
                            entity.setToIdentifyCode(nhiemVuChiTiet.getMaDonViDuocGiao());
                            entity.setToDeptName(nhiemVuChiTiet.getTenCoQuanDuocGiao());
                            edocReceiveMissionHisRepository.saveOrUpdate(jdbcTemplate, entity);
                        }
                    }

                    List<ChiTietNhiemVuGiaoResponseDTO.LichSuDieuChinhThoiHan> lstLichSuDieuChinhThoiHan = nhiemVuChiTiet.getLichSuDieuChinhThoiHan();
                    if(!CollectionUtils.isEmpty(lstLichSuGuiTra)) {
                        for (ChiTietNhiemVuGiaoResponseDTO.LichSuDieuChinhThoiHan dieuChinhThoiHan : lstLichSuDieuChinhThoiHan) {
                            EdocReceiveMissionHisEntity entity = edocReceiveMissionHisRepository.findFirstByHistoryId(jdbcTemplate, CommonUtils.parseStringToLong(dieuChinhThoiHan.getMaLichSuDieuChinhThoiHan()));
                            entity = convertLichSuDieuChinhThoiHanToEdocMissionHisEntity(entity, dieuChinhThoiHan);
                            entity.setEdocReceiveMissionId(mission.getEdocReceiveMissionId());
                            entity.setCode(nhiemVuChiTiet.getMaNhiemVu());
                            entity.setToIdentifyCode(nhiemVuChiTiet.getMaDonViDuocGiao());
                            entity.setToDeptName(nhiemVuChiTiet.getTenCoQuanDuocGiao());
                            edocReceiveMissionHisRepository.saveOrUpdate(jdbcTemplate, entity);
                        }
                    }

                    List<ChiTietNhiemVuGiaoResponseDTO.LichSuDonDoc> lstLichSuDonDoc = nhiemVuChiTiet.getLichSuDonDocNhiemVu();
                    if(!CollectionUtils.isEmpty(lstLichSuGuiTra)) {
                        for (ChiTietNhiemVuGiaoResponseDTO.LichSuDonDoc donDoc : lstLichSuDonDoc) {
                            EdocReceiveMissionHisEntity entity = edocReceiveMissionHisRepository.findFirstByHistoryId(jdbcTemplate, CommonUtils.parseStringToLong(donDoc.getMaLichSuDonDoc()));
                            entity = convertLichSuDonDocToEdocMissionHisEntity(entity, donDoc);
                            entity.setEdocReceiveMissionId(mission.getEdocReceiveMissionId());
                            entity.setCode(nhiemVuChiTiet.getMaNhiemVu());
                            entity.setToIdentifyCode(nhiemVuChiTiet.getMaDonViDuocGiao());
                            entity.setToDeptName(nhiemVuChiTiet.getTenCoQuanDuocGiao());
                            edocReceiveMissionHisRepository.saveOrUpdate(jdbcTemplate, entity);
                        }
                    }
                } catch (Exception ex) {
                    status.setRollbackOnly();
                    throw ex;
                }
            }
        });
    }

    private EdocReceiveMissionHisEntity convertTienDoXuLyToEdocMissionHisEntity(EdocReceiveMissionHisEntity entity, ChiTietNhiemVuGiaoResponseDTO.TienDoXuLy tienDo) {
        if(Objects.isNull(entity) || Objects.isNull(entity.getEdocReceiveMissionHisId())) {
            entity = new EdocReceiveMissionHisEntity();
        }
        entity.setHistoryId(CommonUtils.parseStringToLong(tienDo.getMaLichSuTienDoXuLy()));
        entity.setType(Constants.EDOC_RECEIVE_MISSION_HIS_TYPE.TIEN_DO_XU_LY);
        entity.setReportContent(tienDo.getNoiDung());
        entity.setStatus(CommonUtils.parseStringToLong(tienDo.getMaTrangThai()));
        entity.setUpdateBy(tienDo.getNguoiCapNhat());
        entity.setUpdateTime(CommonUtils.convertStringToDateWithFormat(tienDo.getNgayCapNhat(), "yyyyMMddHHmm"));
        return entity;
    }

    private EdocReceiveMissionHisEntity convertLichSuTraToEdocMissionHisEntity(EdocReceiveMissionHisEntity entity, ChiTietNhiemVuGiaoResponseDTO.LichSuGuiTraLai tienDo) {
        if(Objects.isNull(entity) || Objects.isNull(entity.getEdocReceiveMissionHisId())) {
            entity = new EdocReceiveMissionHisEntity();
        }
        entity.setHistoryId(CommonUtils.parseStringToLong(tienDo.getMaLichSuGuiTra()));
        entity.setType(Constants.EDOC_RECEIVE_MISSION_HIS_TYPE.LICH_SU_GUI_TRA);
        entity.setReportContent(tienDo.getNoiDung());
        entity.setStatus(CommonUtils.parseStringToLong(tienDo.getTrangThaiThuHoiTraLai()));
        entity.setUpdateBy(tienDo.getNguoiGui());
        entity.setUpdateTime(CommonUtils.convertStringToDateWithFormat(tienDo.getNgayThucHien(), "yyyyMMddHHmm"));
        return entity;
    }

    private EdocReceiveMissionHisEntity convertLichSuDonDocToEdocMissionHisEntity(EdocReceiveMissionHisEntity entity, ChiTietNhiemVuGiaoResponseDTO.LichSuDonDoc tienDo) {
        if(Objects.isNull(entity) || Objects.isNull(entity.getEdocReceiveMissionHisId())) {
            entity = new EdocReceiveMissionHisEntity();
        }
        entity.setHistoryId(CommonUtils.parseStringToLong(tienDo.getMaLichSuDonDoc()));
        entity.setType(Constants.EDOC_RECEIVE_MISSION_HIS_TYPE.LICH_SU_DON_DOC);
        entity.setReportContent(tienDo.getNoiDung());
        entity.setStatus(CommonUtils.parseStringToLong(tienDo.getHinhThucDonDoc()));
        entity.setUpdateBy(tienDo.getNguoiDonDoc());
        entity.setUpdateTime(CommonUtils.convertStringToDateWithFormat(tienDo.getNgayDonDoc(), "yyyyMMddHHmm"));
        return entity;
    }

    private EdocReceiveMissionHisEntity convertLichSuDieuChinhThoiHanToEdocMissionHisEntity(EdocReceiveMissionHisEntity entity, ChiTietNhiemVuGiaoResponseDTO.LichSuDieuChinhThoiHan dieuChinhThoiHan) {
        if(Objects.isNull(entity) || Objects.isNull(entity.getEdocReceiveMissionHisId())) {
            entity = new EdocReceiveMissionHisEntity();
        }
        entity.setHistoryId(CommonUtils.parseStringToLong(dieuChinhThoiHan.getMaLichSuDieuChinhThoiHan()));
        entity.setType(Constants.EDOC_RECEIVE_MISSION_HIS_TYPE.LICH_SU_DIEU_CHINH_THOI_HAN);
        entity.setReportContent(dieuChinhThoiHan.getVanBanDieuChinhThoiHan());
        entity.setStatus(CommonUtils.parseStringToLong(dieuChinhThoiHan.getTrangThaiCapNhat()));
        entity.setUpdateBy(dieuChinhThoiHan.getNguoiThucHien());
        entity.setDeadlineOld(CommonUtils.convertStringToDateWithFormat(dieuChinhThoiHan.getHanCu(), "yyyyMMdd"));
        entity.setDeadlineNew(CommonUtils.convertStringToDateWithFormat(dieuChinhThoiHan.getHanMoi(), "yyyyMMdd"));
        entity.setUpdateTime(CommonUtils.convertStringToDateWithFormat(dieuChinhThoiHan.getNgayThucHien(), "yyyyMMdd"));

        return entity;
    }
}
