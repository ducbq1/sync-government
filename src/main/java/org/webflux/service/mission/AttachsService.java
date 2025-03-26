package org.webflux.service.mission;

import org.springframework.stereotype.Service;
import org.webflux.domain.mission.AttachsEntity;
import org.webflux.domain.mission.EdocReceiveFileAttachEntity;
import org.webflux.repository.query.SyncFlowStaticQuery;

import javax.sql.DataSource;

@Service
public interface AttachsService {
    AttachsEntity saveFileDinhKem(DataSource dataSource, EdocReceiveFileAttachEntity eDocFileEntity, String dataBase64, SyncFlowStaticQuery base);
}
