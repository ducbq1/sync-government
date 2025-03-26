package org.webflux.domain.mission;

import lombok.Data;

@Data
public class EdocReceiveFileAttachEntity {
    private Long edocReceiveFileAttachId;
    private Long missionId;
    private Long missionHisId;
    private String fileId;
    private Long typeId;
    private String tenFile;
    private String filePath;
    private Long isSync;
}
