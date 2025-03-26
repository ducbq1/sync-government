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
import org.webflux.domain.mission.CategoryEntity;
import org.webflux.domain.mission.EdocMissionCombineEntity;
import org.webflux.domain.mission.MissionDetailEntity;
import org.webflux.domain.mission.MissionEntity;
import org.webflux.helper.StringUtils;
import org.webflux.model.dto.ChiTietNhiemVuGiaoResponseDTO;
import org.webflux.model.dto.NhiemVuGiaoResponseDTO;
import org.webflux.helper.CommonUtils;
import org.webflux.repository.mission.MissionCategoryRepository;
import org.webflux.repository.mission.MissionDetailRepository;
import org.webflux.repository.mission.MissionRepository;
import org.webflux.service.AuditLogService;
import org.webflux.service.mission.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;

import static org.webflux.enumerate.MissionAction.*;

@Service
public class MissionServiceImpl implements MissionService {
    private MissionRepository missionRepository;
    private MissionCategoryService missionCategoryService;
    private MissionDetailRepository missionDetailRepository;
    private MissionDetailService missionDetailService;
    private MissionHisService missionHisService;
    private EdocReceiveFileAttachService edocReceiveFileAttachService;
    private AuditLogService logService;


    public MissionServiceImpl(MissionRepository missionRepository,
                              MissionCategoryService missionCategoryService,
                              MissionDetailRepository missionDetailRepository,
                              MissionDetailService missionDetailService,
                              MissionHisService missionHisService,
                              AuditLogService logService,
                              EdocReceiveFileAttachService edocReceiveFileAttachService) {
        this.missionRepository = missionRepository;
        this.missionCategoryService = missionCategoryService;
        this.missionDetailRepository = missionDetailRepository;
        this.missionDetailService = missionDetailService;
        this.missionHisService = missionHisService;
        this.edocReceiveFileAttachService = edocReceiveFileAttachService;
        this.logService = logService;
    }

    @Override
//    @Transactional
    public void saveMission(DataSource dataSource, NhiemVuGiaoResponseDTO.NhiemVu nhiemVu, ChiTietNhiemVuGiaoResponseDTO.Item chiTietNhiemVu, String identifyCode, Boolean syncedAgain, String url) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    MissionEntity mission = missionRepository.findByCode(jdbcTemplate, nhiemVu.getMaNhiemVu());
                    var roleUserDept = missionRepository.findByIdentifyCode(jdbcTemplate, identifyCode);
                    var listUserId = missionRepository.findAllReceiverUserId(jdbcTemplate, identifyCode);
                    if (roleUserDept.isEmpty()) return;
                    if (Objects.isNull(roleUserDept.get().getDeptId())) return;

                    if (Objects.nonNull(syncedAgain) && syncedAgain || "0".equals(nhiemVu.getDaDongBo())) {
                        mission = convertMissionDTOToEntity(jdbcTemplate, mission, nhiemVu, chiTietNhiemVu, roleUserDept.get().getDeptId());
                        missionRepository.saveOrUpdate(jdbcTemplate, mission);
                        MissionDetailEntity missionDetail = missionDetailService.saveMissionDetail(dataSource, mission, nhiemVu, chiTietNhiemVu, roleUserDept.get().getDeptId(), listUserId);
                        missionHisService.saveMissionHis(dataSource, mission, missionDetail, chiTietNhiemVu, roleUserDept.get().getUserId(), identifyCode);
                        logService.log(THEM_MOI, mission.getCode(), url, false);
                    } else {

                        if (Objects.nonNull(mission)) {

                            if ("1".equals(nhiemVu.getTrangThaiCapNhat())) {
                                mission = convertMissionDTOToEntity(jdbcTemplate, mission, nhiemVu, chiTietNhiemVu, roleUserDept.get().getDeptId());
                                missionRepository.saveOrUpdate(jdbcTemplate, mission);
                                MissionDetailEntity missionDetail = missionDetailService.saveMissionDetail(dataSource, mission, nhiemVu, chiTietNhiemVu, roleUserDept.get().getDeptId(), listUserId);
                                missionHisService.saveMissionHis(dataSource, mission, missionDetail, chiTietNhiemVu, roleUserDept.get().getUserId(), identifyCode);
                                logService.log(CO_CAP_NHAT, mission.getCode(), url, false);
                            } else if ("2".equals(nhiemVu.getTrangThaiCapNhat())) {
                                MissionEntity newMission = convertMissionDTOToEntity(jdbcTemplate, null, nhiemVu, chiTietNhiemVu, roleUserDept.get().getDeptId());
                                missionRepository.saveOrUpdate(jdbcTemplate, newMission);
                                MissionDetailEntity missionDetail = missionDetailService.saveMissionDetail(dataSource, newMission, nhiemVu, chiTietNhiemVu, roleUserDept.get().getDeptId(), listUserId);
                                missionHisService.saveMissionHis(dataSource, newMission, missionDetail, chiTietNhiemVu, roleUserDept.get().getDeptId(), identifyCode);
                                logService.log(THEM_MOI, mission.getCode(), url, false);
                            } else if ("3".equals(nhiemVu.getTrangThaiCapNhat())) {
                                MissionDetailEntity missionDetail = null;
                                if (Objects.nonNull(mission)) {
                                    missionDetail = missionDetailRepository.findByMissionId(jdbcTemplate, mission.getMissionId());
                                }
                                missionDetail.setState(13L);
                                mission.setStatusUpdate(3L);
                                missionRepository.saveOrUpdate(jdbcTemplate, mission);
                                missionDetailRepository.saveOrUpdate(jdbcTemplate, missionDetail);
                                missionHisService.saveMissionHis(dataSource, mission, missionDetail, chiTietNhiemVu, roleUserDept.get().getDeptId(), identifyCode);
                                logService.log(THU_HOI, mission.getCode(), url, false);
                            }
                        }
                    }
                } catch (Exception ex) {
                    status.setRollbackOnly();
                    throw ex;
                }
            }
        });
    }

    private MissionEntity convertMissionDTOToEntity(JdbcTemplate jdbcTemplate, MissionEntity entity, NhiemVuGiaoResponseDTO.NhiemVu nhiemVu, ChiTietNhiemVuGiaoResponseDTO.Item chiTietNhiemVu, Long deptId) {
        if (Objects.isNull(entity) || Objects.isNull(entity.getMissionId())) {
            entity = new MissionEntity();
        }

        if (chiTietNhiemVu.getThuocCTCT().equals("Có")) {
            var missionCategory = missionCategoryService.exist(jdbcTemplate);
            if (Objects.isNull(missionCategory)) {
                var newMissionCategory = missionCategoryService.insert(jdbcTemplate, CategoryEntity.builder().isActive(true).categoryTypeCode("MISSION_TYPE").name("Nhiệm vụ CTCT").build());
                entity.setMissionTypeId(newMissionCategory.getCategoryId());
            } else {
                entity.setMissionTypeId(missionCategory.getCategoryId());
            }
        }

        entity.setCode(nhiemVu.getMaNhiemVu());
        entity.setContent(chiTietNhiemVu.getNoiDungNhiemVu());
        entity.setStatus(0L);
        entity.setDocumentId(-1L);
        entity.setDocumentType(-1L);
        entity.setCreateTime(CommonUtils.convertStringToSQLDateFormat(chiTietNhiemVu.getThoiGianTaoNhiemVu(), "yyyyMMddHHmm"));
        entity.setDeadline(CommonUtils.convertStringToSQLDateFormat(chiTietNhiemVu.getHanXuLy(), "yyyyMMdd"));
        entity.setFinishTime(null);
        entity.setState(1L);
        entity.setResult(null);
        entity.setComments(null);
        entity.setCreateBy(null);
        entity.setCreateDeptId(deptId);
        entity.setCreateRoleId(null);

        entity.setCreateDeptCode(chiTietNhiemVu.getMaDonViSoanThao());
        entity.setCreateDeptName(chiTietNhiemVu.getDonViSoanThao());

        entity.setDatePublish(CommonUtils.convertStringToSQLDateFormat(chiTietNhiemVu.getVanBanPhatHanh().getNgayVanBan(), "yyyyMMdd"));
        entity.setDocumentCode(chiTietNhiemVu.getVanBanPhatHanh().getSoKyHieu());
        entity.setAbstractDocument(chiTietNhiemVu.getVanBanPhatHanh().getTrichYeu());
        entity.setTypeMission(1L);
        entity.setFollowIdentifyCode(chiTietNhiemVu.getMaDonViTheoDoi());
        entity.setFollowDeptName(chiTietNhiemVu.getDonViTheoDoi());
        entity.setNumberUrge(chiTietNhiemVu.getLichSuDonDocNhiemVu().stream().count());
        entity.setFollowName(chiTietNhiemVu.getChuyenVienTheoDoi());
        entity.setContact(chiTietNhiemVu.getThongTinLienHe());
        entity.setSynchronizedDate(CommonUtils.convertStringToSQLDateFormat(chiTietNhiemVu.getThoiGianDongBo(), "yyyyMMddHHmm"));
        entity.setToIdentifyCode(chiTietNhiemVu.getMaDonViDuocGiao());
        entity.setToDeptName(chiTietNhiemVu.getTenCoQuanDuocGiao());
        entity.setDatePublish(CommonUtils.convertStringToSQLDateFormat(chiTietNhiemVu.getVanBanPhatHanh().getNgayVanBan(), "yyyyMMdd"));
        entity.setSecurityTypeId(CommonUtils.parseStringToLong(chiTietNhiemVu.getVanBanPhatHanh().getDoMat()));
        entity.setPublishAgencyName(chiTietNhiemVu.getVanBanPhatHanh().getDonViPhatHanh());
        entity.setSignerName(chiTietNhiemVu.getVanBanPhatHanh().getNguoiKy());
        entity.setSignerName(chiTietNhiemVu.getVanBanPhatHanh().getChucVu());
        entity.setIsEdoc(1L);
        entity.setStatusSys(CommonUtils.parseStringToLong(nhiemVu.getDaDongBo()));
        entity.setStatusUpdate(CommonUtils.parseStringToLong(nhiemVu.getTrangThaiCapNhat()));
        return entity;
    }

}
