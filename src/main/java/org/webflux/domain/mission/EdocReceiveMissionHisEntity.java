package org.webflux.domain.mission;

import lombok.Data;

import java.util.Date;

@Data
public class EdocReceiveMissionHisEntity {
    private Long edocReceiveMissionHisId;
    private Long edocReceiveMissionId;
    private Long historyId;
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
}
