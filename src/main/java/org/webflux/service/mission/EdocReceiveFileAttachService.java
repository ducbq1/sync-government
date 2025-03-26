package org.webflux.service.mission;

import org.springframework.stereotype.Service;
import org.webflux.domain.mission.EdocReceiveFileAttachEntity;
import org.webflux.domain.mission.MissionEntity;
import org.webflux.domain.mission.MissionHisEntity;
import org.webflux.model.dto.FileDinhKem;

import javax.sql.DataSource;
import java.util.List;

@Service
public interface EdocReceiveFileAttachService {
    EdocReceiveFileAttachEntity saveOrUpdate(DataSource dataSource, MissionEntity mission, MissionHisEntity missionHisEntity, FileDinhKem fileDinhKem, String pathFile);
    List<EdocReceiveFileAttachEntity> findByIsSync(DataSource dataSource, Long isSync, Long maxRow);

}
