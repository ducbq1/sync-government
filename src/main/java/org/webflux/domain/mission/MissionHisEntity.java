package org.webflux.domain.mission;

import freemarker.core.Environment;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;

import java.sql.Date;

@Data
public class MissionHisEntity {
    private Long missionHisId;
    private Long missionId;
    private Long userId;
    private Long actionType;
    private String content;
    private Date createTime;
    private Long receiverId;
    private Long receiverType;
    private Long defaultUserRec;
    private Long detailId;
    private String reportInfo;
    private String fileAttachs;
    private String code;
    private Long type;
    private String reportContent;
    private Long status;
    private Date deadlineOld;
    private Date deadlineNew;
    private Long updateById;
    private String updateBy;
    private Date updateTime;
    private String toIdentifyCode;
    private String toDeptName;
    private Long documentId;
    private String documentCode;
    private String abstractDocument;
    private String isEdoc;
    private Long isSent;
    private Date sendDate;
    private String messageLog;

    public String getCode() {
        if (Strings.isBlank(code)) {
            return Strings.EMPTY;
        }
        return code;
    }
}
