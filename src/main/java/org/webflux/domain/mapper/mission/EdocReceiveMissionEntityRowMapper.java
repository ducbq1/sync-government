package org.webflux.domain.mapper.mission;

import org.springframework.jdbc.core.RowMapper;
import org.webflux.domain.mission.EdocReceiveMissionEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EdocReceiveMissionEntityRowMapper implements RowMapper<EdocReceiveMissionEntity> {
    @Override
    public EdocReceiveMissionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        EdocReceiveMissionEntity entity = new EdocReceiveMissionEntity();

        entity.setEdocReceiveMissionId(rs.getLong("edoc_receive_mission_id"));
        entity.setIdentifyFromCode(rs.getString("identify_from_code"));
        entity.setIdentifyFromName(rs.getString("identify_from_name"));
        entity.setCode(rs.getString("code"));
        entity.setDocumentType(rs.getLong("document_type"));
        entity.setStatusSys(rs.getLong("status_sys"));
        entity.setStatusUpdate(rs.getLong("status_update"));
        entity.setDatePublish(rs.getDate("date_publish"));
        entity.setContent(rs.getString("content"));
        entity.setCtct(rs.getString("ctct"));
        entity.setDocumentCode(rs.getString("document_code"));
        entity.setCreateDeptCode(rs.getString("create_dept_code"));
        entity.setCreateDeptName(rs.getString("create_dept_name"));
        entity.setCreateBy(rs.getString("create_by"));
        entity.setCreateTime(rs.getDate("create_time"));
        entity.setFollowIdentifyCode(rs.getString("follow_identify_code"));
        entity.setFollowDeptName(rs.getString("follow_dept_name"));
        entity.setMissionType(rs.getLong("mission_type"));
        entity.setDeadline(rs.getDate("deadline"));
        entity.setNumberUrge(rs.getLong("number_urge"));
        entity.setTypeMission(rs.getLong("type_mission"));
        entity.setFollowName(rs.getString("follow_name"));
        entity.setContact(rs.getString("contact"));
        entity.setSynchronizedDate(rs.getDate("synchronized_date"));
        entity.setToIdentifyCode(rs.getString("to_identify_code"));
        entity.setToDeptName(rs.getString("to_dept_name"));
        entity.setIsSend(rs.getLong("is_send"));
        entity.setScope(rs.getString("scope"));
        entity.setDocumentId(rs.getLong("document_id"));
        entity.setAbstractContent(rs.getString("abstract"));
        entity.setPublishAgencyName(rs.getString("publish_agency_name"));
        entity.setDateCreate(rs.getDate("date_create"));
        entity.setSecurityTypeId(rs.getLong("security_type_id"));
        entity.setSignerName(rs.getString("signer_name"));
        entity.setPosName(rs.getString("pos_name"));
        entity.setEdxml(rs.getString("edxml"));

        return entity;
    }
}
