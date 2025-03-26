package org.webflux.service.implement.mission;

import io.micrometer.common.util.StringUtils;
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
import org.webflux.domain.mission.MissionDetailEntity;
import org.webflux.domain.mission.MissionEntity;
import org.webflux.domain.mission.MissionHisEntity;
import org.webflux.helper.CommonUtils;
import org.webflux.helper.Constants;
import org.webflux.model.dto.*;
import org.webflux.repository.mission.MissionDetailRepository;
import org.webflux.repository.mission.MissionHisRepository;
import org.webflux.repository.mission.MissionRepository;
import org.webflux.service.mission.EdocReceiveFileAttachService;
import org.webflux.service.mission.MissionHisService;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Date;
import java.util.List;
import java.util.Objects;

@Service
public class MissionHisServiceImpl implements MissionHisService {
    private MissionHisRepository missionHisRepository;
    private MissionRepository missionRepository;
    private EdocReceiveFileAttachService edocReceiveFileAttachService;
    private MissionDetailRepository missionDetailRepository;

    public MissionHisServiceImpl(MissionHisRepository missionHisRepository,
                                 MissionRepository missionRepository,
                                 EdocReceiveFileAttachService edocReceiveFileAttachService,
                                 MissionDetailRepository missionDetailRepository) {
        this.missionHisRepository = missionHisRepository;
        this.missionRepository = missionRepository;
        this.edocReceiveFileAttachService = edocReceiveFileAttachService;
        this.missionDetailRepository = missionDetailRepository;
    }

    @Override
    @Transactional
    public void saveMissionHis(DataSource dataSource, MissionEntity mission, MissionDetailEntity missionDetail, ChiTietNhiemVuGiaoResponseDTO.Item chiTietNhiemVu, Long userId, String identifyCode) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    List<ChiTietNhiemVuGiaoResponseDTO.TienDoXuLy> lstTienDoXuLy = chiTietNhiemVu.getTienDoXuLy();
                    if(!CollectionUtils.isEmpty(lstTienDoXuLy)) {
                        for (ChiTietNhiemVuGiaoResponseDTO.TienDoXuLy tienDo : lstTienDoXuLy) {
                            MissionHisEntity entity = new MissionHisEntity();
                            entity = convertTienDoXuLyToMissionHisEntity(entity, tienDo, userId);
                            entity.setMissionId(mission.getMissionId());
                            entity.setDetailId(missionDetail.getMissionDetailId());
                            entity.setCode(chiTietNhiemVu.getMaNhiemVu());
                            entity.setToIdentifyCode(chiTietNhiemVu.getMaDonViDuocGiao());
                            entity.setToDeptName(chiTietNhiemVu.getTenCoQuanDuocGiao());
                            missionHisRepository.saveOrUpdate(jdbcTemplate, entity);


                            Path path = null;
                            if(Objects.nonNull(tienDo) && !CollectionUtils.isEmpty(tienDo.getFileDinhKem())) {

                                var deptPath = missionRepository.findDeptPath(jdbcTemplate, identifyCode);
                                if (deptPath.isPresent() && StringUtils.isNotEmpty(tienDo.getNgayCapNhat())) {
                                    var x = tienDo.getNgayCapNhat();
                                    path = Path.of("Y".concat(x.substring(0, 4)), deptPath.get(), "M".concat(x.substring(4, 6)), "D".concat(x.substring(6, 8)), "OBJECT_TYPE_99");
                                }

                                for (FileDinhKem fileDinhKem : tienDo.getFileDinhKem()) {
                                    edocReceiveFileAttachService.saveOrUpdate(dataSource, mission, entity, fileDinhKem, path.toString());
                                }
                            }
                        }
                    }

                    List<ChiTietNhiemVuGiaoResponseDTO.LichSuGuiTraLai> lstLichSuGuiTra = chiTietNhiemVu.getLichSuGuiTraLaiBoDiaPhuong();
                    if(!CollectionUtils.isEmpty(lstLichSuGuiTra)) {
                        for (ChiTietNhiemVuGiaoResponseDTO.LichSuGuiTraLai traLai : lstLichSuGuiTra) {
                            MissionHisEntity entity = new MissionHisEntity();
                            entity = convertLichSuTraLaiToMissionHisEntity(entity, traLai, userId);
                            entity.setMissionId(mission.getMissionId());
                            entity.setDetailId(missionDetail.getMissionDetailId());
                            entity.setCode(chiTietNhiemVu.getMaNhiemVu());
                            entity.setToIdentifyCode(chiTietNhiemVu.getMaDonViDuocGiao());
                            entity.setToDeptName(chiTietNhiemVu.getTenCoQuanDuocGiao());
                            missionHisRepository.saveOrUpdate(jdbcTemplate, entity);
                        }
                    }

                    List<ChiTietNhiemVuGiaoResponseDTO.LichSuDonDoc> lstLichSuDonDoc = chiTietNhiemVu.getLichSuDonDocNhiemVu();
                    if(!CollectionUtils.isEmpty(lstLichSuGuiTra)) {
                        for (ChiTietNhiemVuGiaoResponseDTO.LichSuDonDoc donDoc : lstLichSuDonDoc) {
                            MissionHisEntity entity = new MissionHisEntity();
                            entity = convertDonDocToMissionHisEntity(entity, donDoc, userId);
                            entity.setMissionId(mission.getMissionId());
                            entity.setDetailId(missionDetail.getMissionDetailId());
                            entity.setCode(chiTietNhiemVu.getMaNhiemVu());
                            entity.setToIdentifyCode(chiTietNhiemVu.getMaDonViDuocGiao());
                            entity.setToDeptName(chiTietNhiemVu.getTenCoQuanDuocGiao());
                            missionHisRepository.saveOrUpdate(jdbcTemplate, entity);

                            Path path = null;
                            if(Objects.nonNull(donDoc) && !CollectionUtils.isEmpty(donDoc.getFileDinhKem())) {

                                var deptPath = missionRepository.findDeptPath(jdbcTemplate, identifyCode);
                                if (deptPath.isPresent() && StringUtils.isNotEmpty(donDoc.getNgayTao())) {
                                    var x = donDoc.getNgayTao();
                                    path = Path.of("Y".concat(x.substring(0, 4)), deptPath.get(), "M".concat(x.substring(4, 6)), "D".concat(x.substring(6, 8)), "OBJECT_TYPE_99");
                                }

                                for (FileDinhKem fileDinhKem : donDoc.getFileDinhKem()) {
                                    edocReceiveFileAttachService.saveOrUpdate(dataSource, mission, entity, fileDinhKem, path.toString());
                                }
                            }
                        }
                    }

                    List<ChiTietNhiemVuGiaoResponseDTO.LichSuDieuChinhThoiHan> lstLichSuDieuChinhThoiHan = chiTietNhiemVu.getLichSuDieuChinhThoiHan();
                    if(!CollectionUtils.isEmpty(lstLichSuDieuChinhThoiHan)) {
                        for (ChiTietNhiemVuGiaoResponseDTO.LichSuDieuChinhThoiHan dieuChinhThoiHan : lstLichSuDieuChinhThoiHan) {
                            MissionHisEntity entity = new MissionHisEntity();
                            entity = convertDieuChinhThoiHanToMissionHisEntity(entity, dieuChinhThoiHan, userId);
                            entity.setMissionId(mission.getMissionId());
                            entity.setDetailId(missionDetail.getMissionDetailId());
                            entity.setCode(chiTietNhiemVu.getMaNhiemVu());
                            entity.setToIdentifyCode(chiTietNhiemVu.getMaDonViDuocGiao());
                            entity.setToDeptName(chiTietNhiemVu.getTenCoQuanDuocGiao());
                            missionHisRepository.saveOrUpdate(jdbcTemplate, entity);


                            Path path = null;
                            if(Objects.nonNull(dieuChinhThoiHan) && !CollectionUtils.isEmpty(dieuChinhThoiHan.getFileDinhKem())) {

                                var deptPath = missionRepository.findDeptPath(jdbcTemplate, identifyCode);
                                if (deptPath.isPresent() && StringUtils.isNotEmpty(dieuChinhThoiHan.getNgayThucHien())) {
                                    var x = dieuChinhThoiHan.getNgayThucHien();
                                    path = Path.of("Y".concat(x.substring(0, 4)), deptPath.get(), "M".concat(x.substring(4, 6)), "D".concat(x.substring(6, 8)), "OBJECT_TYPE_99");
                                }

                                for (FileDinhKem fileDinhKem : dieuChinhThoiHan.getFileDinhKem()) {
                                    edocReceiveFileAttachService.saveOrUpdate(dataSource, mission, entity, fileDinhKem, path.toString());
                                }
                            }
                        }
                    }

                    var vanBanPhatHanh = chiTietNhiemVu.getVanBanPhatHanh();
                    Path path = null;
                    if(Objects.nonNull(vanBanPhatHanh) && !CollectionUtils.isEmpty(vanBanPhatHanh.getFileDinhKem())) {

                        var deptPath = missionRepository.findDeptPath(jdbcTemplate, identifyCode);
                        if (deptPath.isPresent() && StringUtils.isNotEmpty(vanBanPhatHanh.getNgayVanBan())) {
                            var x = vanBanPhatHanh.getNgayVanBan();
                            path = Path.of("Y".concat(x.substring(0, 4)), deptPath.get(), "M".concat(x.substring(4, 6)), "D".concat(x.substring(6, 8)), "OBJECT_TYPE_99");
                        }

                        for (FileDinhKem fileDinhKem : vanBanPhatHanh.getFileDinhKem()) {
                            edocReceiveFileAttachService.saveOrUpdate(dataSource, mission, new MissionHisEntity(), fileDinhKem, path.toString());
                        }
                    }

                } catch (Exception ex) {
                    status.setRollbackOnly();
                    throw ex;
                }
            }
        });
    }

    private MissionHisEntity convertTienDoXuLyToMissionHisEntity(MissionHisEntity entity, ChiTietNhiemVuGiaoResponseDTO.TienDoXuLy tienDo, Long userId) {
        if(Objects.isNull(entity) || Objects.isNull(entity.getMissionHisId())) {
            entity = new MissionHisEntity();
        }
        entity.setUserId(userId);
        entity.setActionType(1L);
        entity.setContent(tienDo.getNoiDung());
        entity.setCreateTime(new Date(System.currentTimeMillis()));
        entity.setReceiverId(userId);
        entity.setReceiverType(1L);
        entity.setDefaultUserRec(userId);
        entity.setReportInfo(tienDo.getNoiDung());
        entity.setType(Constants.EDOC_RECEIVE_MISSION_HIS_TYPE.TIEN_DO_XU_LY);
        entity.setStatus(CommonUtils.parseStringToLong(tienDo.getMaTrangThai()));
        entity.setDeadlineOld(null);
        entity.setDeadlineNew(null);
        entity.setUpdateById(userId);
        entity.setUpdateBy(tienDo.getNguoiCapNhat());
        entity.setUpdateTime(CommonUtils.convertStringToSQLDateFormat(tienDo.getNgayCapNhat(), "yyyyMMddHHmm"));
        return entity;
    }

    private MissionHisEntity convertLichSuTraLaiToMissionHisEntity(MissionHisEntity entity, ChiTietNhiemVuGiaoResponseDTO.LichSuGuiTraLai traLai, Long userId) {
        if(Objects.isNull(entity) || Objects.isNull(entity.getMissionHisId())) {
            entity = new MissionHisEntity();
        }
        entity.setUserId(userId);
        entity.setActionType(1L);
        entity.setContent(traLai.getNoiDung());
        entity.setCreateTime(new Date(System.currentTimeMillis()));
        entity.setReceiverId(userId);
        entity.setReceiverType(1L);
        entity.setDefaultUserRec(userId);
        entity.setReportInfo(traLai.getNoiDung());
        entity.setType(Constants.EDOC_RECEIVE_MISSION_HIS_TYPE.LICH_SU_GUI_TRA);
        entity.setStatus(CommonUtils.parseStringToLong(traLai.getTrangThaiThuHoiTraLai()));
        entity.setDeadlineOld(null);
        entity.setDeadlineNew(null);
        entity.setUpdateById(userId);
        entity.setUpdateBy(traLai.getNguoiGui());
        entity.setUpdateTime(CommonUtils.convertStringToSQLDateFormat(traLai.getNgayThucHien(), "yyyyMMddHHmm"));
        return entity;
    }

    private MissionHisEntity convertDonDocToMissionHisEntity(MissionHisEntity entity, ChiTietNhiemVuGiaoResponseDTO.LichSuDonDoc donDoc, Long userId) {
        if(Objects.isNull(entity) || Objects.isNull(entity.getMissionHisId())) {
            entity = new MissionHisEntity();
        }
        entity.setUserId(userId);
        entity.setActionType(1L);
        entity.setContent(donDoc.getNoiDung());
        entity.setCreateTime(new Date(System.currentTimeMillis()));
        entity.setReceiverId(userId);
        entity.setReceiverType(1L);
        entity.setDefaultUserRec(userId);
        entity.setReportInfo(donDoc.getNoiDung());
        entity.setType(Constants.EDOC_RECEIVE_MISSION_HIS_TYPE.LICH_SU_DON_DOC);
        entity.setStatus(CommonUtils.parseStringToLong(donDoc.getHinhThucDonDoc()));
        entity.setDeadlineOld(null);
        entity.setDeadlineNew(null);
        entity.setUpdateById(userId);
        entity.setUpdateBy(donDoc.getNguoiDonDoc());
        entity.setUpdateTime(CommonUtils.convertStringToSQLDateFormat(donDoc.getNgayDonDoc(), "yyyyMMddHHmm"));
        return entity;
    }

    private MissionHisEntity convertDieuChinhThoiHanToMissionHisEntity(MissionHisEntity entity, ChiTietNhiemVuGiaoResponseDTO.LichSuDieuChinhThoiHan dieuChinhThoiHan, Long userId) {
        if(Objects.isNull(entity) || Objects.isNull(entity.getMissionHisId())) {
            entity = new MissionHisEntity();
        }
        entity.setUserId(userId);
        entity.setActionType(1L);
        entity.setContent(dieuChinhThoiHan.getVanBanDieuChinhThoiHan());
        entity.setCreateTime(new Date(System.currentTimeMillis()));
        entity.setReceiverId(userId);
        entity.setReceiverType(1L);
        entity.setDefaultUserRec(userId);
        entity.setReportInfo(dieuChinhThoiHan.getVanBanDieuChinhThoiHan());
        entity.setType(Constants.EDOC_RECEIVE_MISSION_HIS_TYPE.LICH_SU_DIEU_CHINH_THOI_HAN);
        entity.setStatus(CommonUtils.parseStringToLong(dieuChinhThoiHan.getTrangThaiCapNhat()));
        entity.setDeadlineOld(CommonUtils.convertStringToSQLDateFormat(dieuChinhThoiHan.getHanCu(), "yyyyMMdd"));
        entity.setDeadlineNew(CommonUtils.convertStringToSQLDateFormat(dieuChinhThoiHan.getHanMoi(), "yyyyMMdd"));
        entity.setUpdateById(userId);
        entity.setUpdateBy(dieuChinhThoiHan.getNguoiThucHien());
        entity.setUpdateTime(CommonUtils.convertStringToSQLDateFormat(dieuChinhThoiHan.getNgayThucHien(), "yyyyMMdd"));
        return entity;
    }

    public MissionHisEntity saveOrUpdate(DataSource dataSource, MissionHisEntity entity) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return missionHisRepository.saveOrUpdate(jdbcTemplate, entity);
    }

    public MissionHisEntity findById(DataSource dataSource, Long id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return missionHisRepository.findById(jdbcTemplate, id);
    }

    public List<String> getStringTraLaiNhiemVu(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return missionHisRepository.getStringTraLaiNhiemVu(jdbcTemplate);
    }

    public List<TraLaiNhiemVuRequestDTO> getBodyTraLaiNhiemVu(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return missionHisRepository.getBodyTraLaiNhiemVu(jdbcTemplate);
    }

    public List<GuiBaoCaoNhiemVuRequestDTO> getBodyGuiBaoCao(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return missionHisRepository.getBodyGuiBaoCao(jdbcTemplate);
    }

    public List<TienDoXuLyNhiemVuRequestDTO> getBodyTienDoXuLy(DataSource dataSource, String basePath) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return missionHisRepository.getBodyTienDoXuLy(jdbcTemplate, basePath);
    }

    public List<DieuChinhThoiGianNhiemVuDTO> getBodyDieuChinhThoiGianNhiemVu(DataSource dataSource, String basePath) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return missionHisRepository.getBodyDieuChinhThoiGianNhiemVu(jdbcTemplate, basePath);
    }

}
