package org.webflux.service.implement.mission;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.webflux.domain.mission.AttachsEntity;
import org.webflux.domain.mission.EdocReceiveFileAttachEntity;
import org.webflux.domain.mission.MissionHisEntity;
import org.webflux.helper.CommonUtils;
import org.webflux.repository.mission.AttachsRepository;
import org.webflux.repository.mission.EdocReceiveFileAttachRepository;
import org.webflux.repository.mission.MissionHisRepository;
import org.webflux.repository.query.SyncFlowStaticQuery;
import org.webflux.service.AuditLogService;
import org.webflux.service.mission.AttachsService;
import org.webflux.service.mission.MissionHisService;

import javax.sql.DataSource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.util.Base64;
import java.util.Calendar;
import java.util.Objects;

import static org.webflux.enumerate.MissionAction.TAI_TEP_TIN;

@Service
public class AttachsServiceImpl implements AttachsService {
    private AttachsRepository attachsRepository;
    private EdocReceiveFileAttachRepository edocReceiveFileAttachRepository;
    private MissionHisRepository missionHisRepository;
    private AuditLogService logService;

    public AttachsServiceImpl(AttachsRepository attachsRepository, EdocReceiveFileAttachRepository edocReceiveFileAttachRepository,
                              MissionHisRepository missionHisRepository, AuditLogService logService) {
        this.attachsRepository = attachsRepository;
        this.edocReceiveFileAttachRepository = edocReceiveFileAttachRepository;
        this.missionHisRepository = missionHisRepository;
        this.logService = logService;
    }

    @Override
    public AttachsEntity saveFileDinhKem(DataSource dataSource, EdocReceiveFileAttachEntity eDocFileEntity, String dataBase64, SyncFlowStaticQuery base) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(status -> {
            try {
                AttachsEntity attachsEntity = attachsRepository.findByFileIdAndObjectId(jdbcTemplate, CommonUtils.parseStringToLong(eDocFileEntity.getFileId()), eDocFileEntity.getMissionId());
                if (Objects.isNull(attachsEntity)) {
                    String filePath = createFileOnServer(eDocFileEntity, dataBase64, base);
                    if (Strings.isNotEmpty(filePath)) {
                        attachsEntity = new AttachsEntity();
                        attachsEntity.setObjectId(eDocFileEntity.getMissionId());
                        attachsEntity.setObjectType(99L);
                        attachsEntity.setAttachName(eDocFileEntity.getTenFile());
                        attachsEntity.setAttachPath(filePath);
                        attachsEntity.setIsActive(true);
                        attachsEntity.setCreatorId(999L);
                        attachsEntity.setDateCreate(new Date(System.currentTimeMillis()));
                        attachsEntity.setAttachType(2L);
                        attachsEntity.setIdFileDinhKem(Long.valueOf(eDocFileEntity.getFileId()));
                        attachsRepository.saveOrUpdate(jdbcTemplate, attachsEntity);

                        //update bang mission his fileattach
                        MissionHisEntity missionHisEntity = missionHisRepository.findById(jdbcTemplate, eDocFileEntity.getMissionHisId());
                        if (Objects.nonNull(missionHisEntity)) {
                            if (Objects.nonNull(missionHisEntity.getFileAttachs())) {
                                missionHisEntity.setFileAttachs(String.join(",", missionHisEntity.getFileAttachs(), attachsEntity.getAttachId().toString()));
                            } else {
                                missionHisEntity.setFileAttachs(attachsEntity.getAttachId().toString());
                            }
                            missionHisRepository.saveOrUpdate(jdbcTemplate, missionHisEntity);
                        }
                        //update edoc file
                        eDocFileEntity.setIsSync(1L);
                        edocReceiveFileAttachRepository.saveOrUpdate(jdbcTemplate, eDocFileEntity);
                    }
                }
                return attachsEntity;
            } catch (Exception ex) {
                status.setRollbackOnly();
                throw ex;
            }
        });

    }

    private String createFileOnServer(EdocReceiveFileAttachEntity entity, String dataBase64, SyncFlowStaticQuery base) {
        try {
//            String home = System.getProperty("user.home");
            String dir = base.getSaveFilePath();
            Path folderSave = Path.of(dir, entity.getFilePath());
            Files.createDirectories(folderSave);

            String fileName = entity.getTenFile();
            String fullPath = folderSave + File.separator + fileName;

            try (FileOutputStream fos = new FileOutputStream(fullPath)) {
                fos.write(Base64.getDecoder().decode(dataBase64));
            }
            logService.log(TAI_TEP_TIN, fullPath, base.getUrl(), false);
            return entity.getFilePath() + File.separator + fileName;
        } catch (Exception ex) {
            return null;
        }
    }
}
