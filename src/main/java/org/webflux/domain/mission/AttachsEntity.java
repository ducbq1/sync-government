package org.webflux.domain.mission;

import lombok.Data;

import java.sql.Date;

@Data
public class AttachsEntity {
    private Long attachId;
    private Long objectId;
    private Long objectType;
    private String attachName;
    private String attachPath;
    private Boolean isActive;
    private Long version;
    private Long creatorId;
    private Long modifierId;
    private Date dateCreate;
    private Date dateModify;
    private Long attachType;
    private Boolean isSigned;
    private Boolean isPublished;
    private String modifierName;
    private String signerIds;
    private Long cloneAttachId;
    private String flag;
    private String noteId;
    private String oldAttachPath;
    private Boolean isBackup;
    private Boolean isComment;
    private String signerCa;
    private Boolean isNote;
    private Long signerCaVersion;
    private String savePath;
    private Long attacksOrder;
    private Boolean isEncrypt;
    private Long oldDocId;
    private Boolean caChecked;
    private Long draftType;
    private Date lastUpdate;
    private Long idFileDinhKem;
}
