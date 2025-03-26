package org.webflux.domain.mission;

import lombok.Data;

import java.sql.Date;

@Data
public class MissionDetailEntity {
    private Long missionDetailId;
    private Long missionId;
    private Long missionType;
    private Date sendTime;
    private Long receiverUserId;
    private Long receiverDeptId;
    private Long receiverRoleId;
    private Long state;
    private Boolean isActive;
    private Boolean isRead;
    private Date finishTime;
    private Long result;
    private String reportContent;
    private Long backState;
    private Long sendUserId;
    private Long sendDeptId;
    private Long sendRoleId;
    private Long parentId;
    private Date deadline;
    private String content;
    private String defaultUsersReceiver;
    private Long personApproval;
    private Long isTaskMaster;
    private Long isDept;
    private Long viewBy;
    private Boolean isEdoc;
    private Boolean isSend;
    private Boolean waitingAcceptDeadline;
    private Long next;
    private Long back;
    private Integer nodeId;
}
