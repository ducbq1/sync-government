package org.webflux.domain.mission;

import lombok.Data;
@Data
public class EdocMissionCombineEntity {
    private Long missionCombineId;
    private Long edocReceiveMissionId;
    private Long missionId;
    private String code;
    private String combineCode;
    private String combineName;
    private Long type;
}
