package org.webflux.service.implement.mission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.webflux.domain.mission.EdocMissionCombineEntity;
import org.webflux.domain.mission.EdocReceiveFileAttachEntity;
import org.webflux.domain.mission.MissionEntity;
import org.webflux.domain.mission.MissionHisEntity;
import org.webflux.model.dto.ChiTietNhiemVuGiaoResponseDTO;
import org.webflux.model.dto.FileDinhKem;
import org.webflux.helper.CommonUtils;
import org.webflux.repository.mission.EdocReceiveFileAttachRepository;
import org.webflux.service.mission.EdocReceiveFileAttachService;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;

@Service
public class EdocReceiveFileAttachServiceImpl implements EdocReceiveFileAttachService {

    private EdocReceiveFileAttachRepository edocReceiveFileAttachRepository;

    public EdocReceiveFileAttachServiceImpl(EdocReceiveFileAttachRepository edocReceiveFileAttachRepository) {
        this.edocReceiveFileAttachRepository = edocReceiveFileAttachRepository;
    }

    @Override
    public EdocReceiveFileAttachEntity saveOrUpdate(DataSource dataSource, MissionEntity mission, MissionHisEntity missionHisEntity, FileDinhKem fileDinhKem, String pathFile) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate.execute(status -> {
            try {
                EdocReceiveFileAttachEntity entity = edocReceiveFileAttachRepository.findByFileIdAndMissionId(jdbcTemplate, Long.valueOf(fileDinhKem.getFileId()), mission.getMissionId());
                entity = convertFileDinhKemToEdocAttach(entity, fileDinhKem);
                entity.setMissionId(mission.getMissionId());
                entity.setFilePath(pathFile);
                if (Objects.nonNull(missionHisEntity) && Objects.nonNull(missionHisEntity.getMissionHisId())) {
                    entity.setMissionHisId(missionHisEntity.getMissionHisId());
                }
                edocReceiveFileAttachRepository.saveOrUpdate(jdbcTemplate, entity);
                return entity;
            } catch (Exception ex) {
                status.setRollbackOnly();
                throw ex;
            }
        });

    }

    @Override
    public List<EdocReceiveFileAttachEntity> findByIsSync(DataSource dataSource, Long isSync, Long maxRow) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return edocReceiveFileAttachRepository.findByIsSync(jdbcTemplate, isSync, maxRow);
    }

    private EdocReceiveFileAttachEntity convertFileDinhKemToEdocAttach(EdocReceiveFileAttachEntity entity, FileDinhKem fileDinhKem) {
        if(Objects.isNull(entity) || Objects.isNull(entity.getEdocReceiveFileAttachId())) {
            entity = new EdocReceiveFileAttachEntity();
        }
        entity.setFileId(fileDinhKem.getFileId());
        entity.setTypeId(CommonUtils.parseStringToLong(fileDinhKem.getTypeId()));
        entity.setTenFile(fileDinhKem.getTenFile());
        entity.setIsSync(0L);
        return entity;
    }
}
