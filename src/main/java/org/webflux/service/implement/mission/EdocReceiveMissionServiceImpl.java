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
import org.webflux.model.dto.ChiTietNhiemVuGiaoResponseDTO;
import org.webflux.model.dto.NhiemVuGiaoResponseDTO;
import org.webflux.repository.mission.EdocReceiveMissionRepository;
import org.webflux.service.mission.EdocMissionCombineService;
import org.webflux.service.mission.EdocReceiveMissionHisService;
import org.webflux.service.mission.EdocReceiveMissionService;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;

import static org.webflux.helper.CommonUtils.convertStringToDateWithFormat;
import static org.webflux.helper.CommonUtils.parseStringToLong;

@Service
public class EdocReceiveMissionServiceImpl implements EdocReceiveMissionService {

    private EdocReceiveMissionRepository edocReceiveMissionRepository;
    private EdocReceiveMissionHisService edocReceiveMissionHisService;
    private EdocMissionCombineService edocMissionCombineService;

    public EdocReceiveMissionServiceImpl(EdocReceiveMissionRepository edocReceiveMissionRepository,
                                         EdocReceiveMissionHisService edocReceiveMissionHisService,
                                         EdocMissionCombineService edocMissionCombineService) {
        this.edocReceiveMissionRepository = edocReceiveMissionRepository;
        this.edocReceiveMissionHisService = edocReceiveMissionHisService;
        this.edocMissionCombineService = edocMissionCombineService;
    }

    @Override
//    @Transactional
    public void saveEdocReceiveMission(DataSource dataSource, NhiemVuGiaoResponseDTO.NhiemVu nhiemVu) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    EdocReceiveMissionEntity entity = edocReceiveMissionRepository.findFirstByCode(jdbcTemplate, nhiemVu.getMaNhiemVu());
                    entity = convertEdocReceiveMissionDTOToEntity(entity, nhiemVu);
                    edocReceiveMissionRepository.saveOrUpdate(jdbcTemplate, entity);
                } catch (Exception ex) {
                    status.setRollbackOnly();
                    throw ex;
                }
            }
        });
    }

    @Override
//    @Transactional
    public void saveEdocReceiveMissionDetail(DataSource dataSource, ChiTietNhiemVuGiaoResponseDTO.Item nhiemVuChiTiet, String maDonViDuocGiao)  {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    EdocReceiveMissionEntity entity = edocReceiveMissionRepository.findFirstByCode(jdbcTemplate, nhiemVuChiTiet.getMaNhiemVu());
                    if(Objects.nonNull(entity)) {
                        entity = convertEdocReceiveMissionDetailDTOToEntity(entity, nhiemVuChiTiet);
                        edocReceiveMissionRepository.saveOrUpdate(jdbcTemplate, entity);
                        edocReceiveMissionHisService.saveEdocReceiveMissionHis(dataSource, nhiemVuChiTiet, entity);
                        edocMissionCombineService.saveEdocMissionCombine(dataSource, nhiemVuChiTiet, entity, maDonViDuocGiao);
                    }
                } catch (Exception ex) {
                    status.setRollbackOnly();
                }
            }
        });
    }

    @Override
    public List<EdocReceiveMissionEntity> getAllMissionByStatusSys(DataSource dataSource, Long statusSys) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<EdocReceiveMissionEntity> results = edocReceiveMissionRepository.findAllByStatusSys(jdbcTemplate, statusSys);
        return results;
    }

    private EdocReceiveMissionEntity convertEdocReceiveMissionDTOToEntity(EdocReceiveMissionEntity entity, NhiemVuGiaoResponseDTO.NhiemVu nhiemVu) {
        if(Objects.isNull(entity) || Objects.isNull(entity.getEdocReceiveMissionId())) {
            entity = new EdocReceiveMissionEntity();
            entity.setStatusSys(1L);
        }
        entity.setCode(nhiemVu.getMaNhiemVu());
        entity.setDocumentType(parseStringToLong(nhiemVu.getLoaiNhiemVu()));
        entity.setStatusUpdate(parseStringToLong(nhiemVu.getTrangThaiCapNhat()));
        return entity;
    }

    private EdocReceiveMissionEntity convertEdocReceiveMissionDetailDTOToEntity(EdocReceiveMissionEntity entity, ChiTietNhiemVuGiaoResponseDTO.Item chiTiet) {
        entity.setStatusSys(1L);
        entity.setCode(chiTiet.getMaNhiemVu());
        entity.setStatusUpdate(parseStringToLong(chiTiet.getTrangThaiCapNhat()));
        entity.setContent(chiTiet.getNoiDungNhiemVu());
        entity.setCtct(chiTiet.getThuocCTCT());
        entity.setDocumentCode(chiTiet.getMaVanBan());
        entity.setCreateDeptCode(chiTiet.getMaDonViSoanThao());
        entity.setCreateDeptName(chiTiet.getDonViSoanThao());
        entity.setCreateBy(chiTiet.getChuyenVienSoanThao());
        entity.setCreateTime(convertStringToDateWithFormat(chiTiet.getThoiGianTaoNhiemVu(), "yyyyMMddHHmm"));
        entity.setFollowIdentifyCode(chiTiet.getMaDonViTheoDoi());
        entity.setFollowDeptName(chiTiet.getDonViTheoDoi());
        entity.setMissionType(parseStringToLong(chiTiet.getLoaiThoiHanNhiemVu()));
        entity.setDeadline(convertStringToDateWithFormat(chiTiet.getHanXuLy(),"yyyyMMdd"));
        entity.setNumberUrge(parseStringToLong(chiTiet.getSoLanDonDoc()));
        entity.setTypeMission(parseStringToLong(chiTiet.getLoai()));
        entity.setFollowName(chiTiet.getChuyenVienTheoDoi());
        entity.setContact(chiTiet.getThongTinLienHe());
        entity.setSynchronizedDate(convertStringToDateWithFormat(chiTiet.getThoiGianDongBo(), "yyyyMMddHHmm"));
        entity.setToIdentifyCode(chiTiet.getMaDonViDuocGiao());
        entity.setToDeptName(chiTiet.getTenCoQuanDuocGiao());
        entity.setScope(chiTiet.getPhamViCapNhat());
        entity.setDocumentCode(chiTiet.getVanBanPhatHanh().getSoKyHieu());
        entity.setAbstractContent(chiTiet.getVanBanPhatHanh().getTrichYeu());
        entity.setPublishAgencyName(chiTiet.getVanBanPhatHanh().getDonViPhatHanh());
        entity.setDatePublish(convertStringToDateWithFormat(chiTiet.getVanBanPhatHanh().getNgayVanBan(), "yyyyMMdd"));
        entity.setSecurityTypeId(parseStringToLong(chiTiet.getVanBanPhatHanh().getDoMat()));
        entity.setSignerName(chiTiet.getVanBanPhatHanh().getNguoiKy());
        entity.setPosName(chiTiet.getVanBanPhatHanh().getChucVu());
        entity.setIdentifyFromCode(chiTiet.getMaDonViSoanThao());
        entity.setIdentifyFromName(chiTiet.getDonViSoanThao());
        entity.setEdxml("");
        return entity;
    }
}
