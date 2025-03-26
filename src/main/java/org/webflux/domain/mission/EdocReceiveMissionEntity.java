package org.webflux.domain.mission;

import lombok.Data;

import java.util.Date;

@Data
public class EdocReceiveMissionEntity {
    private Long edocReceiveMissionId;
    private String identifyFromCode;
    private String identifyFromName;
    private String code;
    private Long documentType;
    private Long statusSys;
    private Long statusUpdate;
    private Date datePublish;
    private String content;
    private String ctct;
    private String documentCode;
    private String createDeptCode;
    private String createDeptName;
    private String createBy;
    private Date createTime;
    private String followIdentifyCode;
    private String followDeptName;
    private Long missionType;
    private Date deadline;
    private Long numberUrge;
    private Long typeMission;
    private String followName;
    private String contact;
    private Date synchronizedDate;
    private String toIdentifyCode;
    private String toDeptName;
    private Long isSend;
    private String scope;
    private Long documentId;
    private String abstractContent;
    private String publishAgencyName;
    private Date dateCreate;
    private Long securityTypeId;
    private String signerName;
    private String posName;
    private String edxml;
}
