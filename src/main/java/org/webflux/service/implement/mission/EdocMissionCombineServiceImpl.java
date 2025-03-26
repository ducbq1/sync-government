package org.webflux.service.implement.mission;

import org.apache.logging.log4j.util.Strings;
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
import org.webflux.domain.mission.*;
import org.webflux.helper.CommonUtils;
import org.webflux.model.dto.ChiTietNhiemVuGiaoResponseDTO;
import org.webflux.repository.mission.EdocMissionCombineRepository;
import org.webflux.repository.mission.MissionRepository;
import org.webflux.service.mission.EdocMissionCombineService;

import javax.sql.DataSource;
import java.sql.Date;
import java.util.List;
import java.util.Objects;

@Service
public class EdocMissionCombineServiceImpl implements EdocMissionCombineService {

    private EdocMissionCombineRepository edocMissionCombineRepository;
    private MissionRepository missionRepository;

    public EdocMissionCombineServiceImpl(EdocMissionCombineRepository edocMissionCombineRepository,
                                         MissionRepository missionRepository) {
        this.edocMissionCombineRepository = edocMissionCombineRepository;
        this.missionRepository = missionRepository;
    }

    @Override
//    @Transactional
    public void saveEdocMissionCombine(DataSource dataSource, ChiTietNhiemVuGiaoResponseDTO.Item nhiemVuChiTiet, EdocReceiveMissionEntity edocMission, String maDonViDuocGiao) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    List<ChiTietNhiemVuGiaoResponseDTO.DonViPhoiHop> lstPhoiHop = nhiemVuChiTiet.getDanhSachDonViPhoiHop();
                    MissionEntity mission = missionRepository.findByCode(jdbcTemplate, nhiemVuChiTiet.getMaNhiemVu());
                    if(!CollectionUtils.isEmpty(lstPhoiHop)) {
                        for (ChiTietNhiemVuGiaoResponseDTO.DonViPhoiHop phoiHop : lstPhoiHop) {
                            if(edocMission.getDocumentType() == 1L || edocMission.getDocumentType() == 2L && maDonViDuocGiao.equals(phoiHop.getMaCoQuan())) {
                                EdocMissionCombineEntity entity = edocMissionCombineRepository.findFirstByEdocReceiveMissionIdCodeCombineCode(jdbcTemplate, edocMission.getEdocReceiveMissionId(), edocMission.getCode(), phoiHop.getMaCoQuan(), phoiHop.getCoQuan());
                                entity = convertDonViPhoiHopToEdocMissionCombineEntity(entity, phoiHop);
                                entity.setEdocReceiveMissionId(edocMission.getEdocReceiveMissionId());
                                entity.setCode(edocMission.getCode());
                                if(edocMission.getDocumentType() == 2L) {
                                    entity.setType(1L);
                                } else {
                                    entity.setType(0L);
                                }
                                if(Objects.nonNull(mission)) {
                                    entity.setMissionId(mission.getMissionId());
                                }
                                edocMissionCombineRepository.saveOrUpdate(jdbcTemplate, entity);
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

    private EdocMissionCombineEntity convertDonViPhoiHopToEdocMissionCombineEntity(EdocMissionCombineEntity entity, ChiTietNhiemVuGiaoResponseDTO.DonViPhoiHop phoiHop) {
        if(Objects.isNull(entity) || Objects.isNull(entity.getMissionCombineId())) {
            entity = new EdocMissionCombineEntity();
        }
        entity.setCombineCode(phoiHop.getMaCoQuan());
        entity.setCombineName(phoiHop.getCoQuan());
        return entity;
    }
}
