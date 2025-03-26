package org.webflux.domain.mission;

import lombok.Data;

import java.sql.Date;

@Data
public class MissionEntity {
    private Long missionId;
    private String content;
    private Long status;
    private Long documentId;
    private Long documentType;
    private Date createTime;
    private Long createBy;
    private Long createDeptId;
    private Long createRoleId;
    private Date deadline;
    private Date finishTime;
    private Long result;
    private String comments;
    private String documentCode;
    private String abstractDocument;
    private Long isEdoc;
    private String code;
    private Date datePublish;
    private Long state;
    private Long missionTypeId;
    private Long statusUpdate;
    private String createDeptCode;
    private String createDeptName;
    private String followDeptName;
    private Long numberUrge;
    private Long typeMission;
    private String followName;
    private String contact;
    private Date synchronizedDate;
    private String toIdentifyCode;
    private String toDeptName;
    private Long securityTypeId;
    private String publishAgencyName;
    private String signerName;
    private String posName;
    private Long typeUrge;
    private Long statusSys;
    private Long importantTypeId;
    private String followIdentifyCode;
}
