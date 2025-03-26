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
import org.webflux.domain.mission.EdocReceiveFileAttachEntity;
import org.webflux.domain.mission.EdocReceiveMissionEntity;
import org.webflux.domain.mission.MissionDetailEntity;
import org.webflux.domain.mission.MissionEntity;
import org.webflux.model.dto.ChiTietNhiemVuGiaoResponseDTO;
import org.webflux.helper.CommonUtils;
import org.webflux.model.dto.NhiemVuGiaoResponseDTO;
import org.webflux.repository.mission.MissionDetailRepository;
import org.webflux.service.mission.MissionDetailService;

import javax.sql.DataSource;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class MissionDetailServiceImpl implements MissionDetailService {
    private MissionDetailRepository missionDetailRepository;

    public MissionDetailServiceImpl(MissionDetailRepository missionDetailRepository) {
        this.missionDetailRepository = missionDetailRepository;
    }

    @Override
    public MissionDetailEntity saveOrUpdate(JdbcTemplate jdbcTemplate, MissionDetailEntity entity) {
        return missionDetailRepository.saveOrUpdate(jdbcTemplate, entity);
    }

    @Override
//    @Transactional
    public MissionDetailEntity saveMissionDetail(DataSource dataSource, MissionEntity mission, NhiemVuGiaoResponseDTO.NhiemVu nhiemVu, ChiTietNhiemVuGiaoResponseDTO.Item chiTietNhiemVu, Long deptId, List<Long> lstUserReceiverId) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate.execute(status -> {
            try {
                MissionDetailEntity missionDetail = missionDetailRepository.findByMissionId(jdbcTemplate, mission.getMissionId());
                missionDetail = convertDTOToMissionDetailEntity(missionDetail, chiTietNhiemVu, deptId);
                if(!CollectionUtils.isEmpty(lstUserReceiverId)) {
                    missionDetail.setReceiverUserId(lstUserReceiverId.get(0));
                    missionDetail.setDefaultUsersReceiver(lstUserReceiverId.stream().skip(1).map(String::valueOf).collect(Collectors.joining(",")));
                }
                missionDetail.setMissionType("1".equals(nhiemVu.getLoaiNhiemVu()) ? 0L : 1L);
                missionDetail.setMissionId(mission.getMissionId());
                missionDetail = missionDetailRepository.saveOrUpdate(jdbcTemplate, missionDetail);

                return missionDetail;
            } catch (Exception ex) {
                status.setRollbackOnly();
                throw ex;
            }
        });
    }

    public List<MissionDetailEntity> findAll(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return missionDetailRepository.findAll(jdbcTemplate);
    }

    public List<MissionDetailEntity> findByCondition(DataSource dataSource, Predicate<MissionDetailEntity> condition) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return missionDetailRepository.findByCondition(jdbcTemplate, condition);
    }

    public List<MissionDetailEntity> findByCondition(DataSource dataSource, Map<String, Object>... condition) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return missionDetailRepository.findByCondition(jdbcTemplate, condition);
    }


    private MissionDetailEntity convertDTOToMissionDetailEntity(MissionDetailEntity missionDetail, ChiTietNhiemVuGiaoResponseDTO.Item chiTietNhiemVu, Long deptId) {
        if (Objects.isNull(missionDetail) || Objects.isNull(missionDetail.getMissionDetailId())) {
            missionDetail = new MissionDetailEntity();
        }

        missionDetail.setSendTime(new Date(System.currentTimeMillis()));
//        missionDetail.setReceiverUserId(null);
        missionDetail.setReceiverDeptId(deptId);
        missionDetail.setReceiverRoleId(null);
        Long state = null;
        if ("0".equals(chiTietNhiemVu.getMaTrangThai())
                || "1".equals(chiTietNhiemVu.getMaTrangThai())) {
            state = 0L;
        } else if ("2".equals(chiTietNhiemVu.getMaTrangThai())) {
            state = 1L;
        } else if ("3".equals(chiTietNhiemVu.getMaTrangThai())) {
            state = 4L;
        } else if ("4".equals(chiTietNhiemVu.getMaTrangThai())) {
            state = 6L;
        }
        missionDetail.setState(state);
        missionDetail.setIsActive(true);

        Optional<ChiTietNhiemVuGiaoResponseDTO.TienDoXuLy> tienDoLastest = getTienDoXLMoiNhat(chiTietNhiemVu.getTienDoXuLy());
        if ("4".equals(chiTietNhiemVu.getMaTrangThai())) {
            if (tienDoLastest.isPresent()) {
                missionDetail.setFinishTime(CommonUtils.convertStringToSQLDateFormat(tienDoLastest.get().getNgayCapNhat(), "yyyyMMddHHmm"));
            }
        }
        missionDetail.setResult(0L);
        if (tienDoLastest.isPresent()) {
            missionDetail.setReportContent(tienDoLastest.get().getNoiDung());
        }
        missionDetail.setBackState(0L);
        missionDetail.setSendUserId(null);
        missionDetail.setSendDeptId(null);
        missionDetail.setSendRoleId(null);
        missionDetail.setParentId(null);
        missionDetail.setDeadline(CommonUtils.convertStringToSQLDateFormat(chiTietNhiemVu.getHanXuLy(), "yyyyMMdd"));
        missionDetail.setContent(chiTietNhiemVu.getNoiDungNhiemVu());
        missionDetail.setDefaultUsersReceiver(null);
        missionDetail.setPersonApproval(null);
        missionDetail.setIsTaskMaster(-1L);
        missionDetail.setIsDept(1L);
        missionDetail.setNext(null);
        missionDetail.setBack(null);
        missionDetail.setViewBy(null);
        missionDetail.setIsEdoc(true);
        missionDetail.setIsSend(null);
        return missionDetail;
    }

    public static Optional<ChiTietNhiemVuGiaoResponseDTO.TienDoXuLy> getTienDoXLMoiNhat(List<ChiTietNhiemVuGiaoResponseDTO.TienDoXuLy> list) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");

        return list.stream()
                .filter(tienDoXuLy -> Objects.nonNull(tienDoXuLy.getNgayCapNhat()))
                .max(Comparator.comparing(tienDoXuLy -> {
                    try {
                        return sdf.parse(tienDoXuLy.getNgayCapNhat());
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                        return new Date(0); // Return epoch date if parsing fails
                    }
                }));
    }
}
