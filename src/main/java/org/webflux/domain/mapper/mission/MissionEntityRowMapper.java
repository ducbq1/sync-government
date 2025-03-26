package org.webflux.domain.mapper.mission;

import org.springframework.jdbc.core.RowMapper;
import org.webflux.domain.mission.MissionEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MissionEntityRowMapper implements RowMapper<MissionEntity> {

    @Override
    public MissionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        MissionEntity entity = new MissionEntity();
        entity.setMissionId(rs.getLong("mission_id"));
        entity.setContent(rs.getString("content"));
        entity.setStatus(rs.getLong("status"));
        entity.setDocumentId(rs.getLong("document_id"));
        entity.setDocumentType(rs.getLong("document_type"));
        entity.setCreateTime(rs.getDate("create_time"));
        entity.setCreateBy(rs.getLong("create_by"));
        entity.setCreateDeptId(rs.getLong("create_dept_id"));
        entity.setCreateRoleId(rs.getLong("create_role_id"));
        entity.setDeadline(rs.getDate("deadline"));
        entity.setFinishTime(rs.getDate("finish_time"));
        entity.setResult(rs.getLong("result"));
        entity.setComments(rs.getString("comments"));
        entity.setDocumentCode(rs.getString("document_code"));
        entity.setAbstractDocument(rs.getString("abstract_document"));
        entity.setIsEdoc(rs.getLong("is_edoc"));
        entity.setCode(rs.getString("code"));
        entity.setDatePublish(rs.getDate("date_publish"));
        entity.setState(rs.getLong("state"));
        entity.setMissionTypeId(rs.getLong("mission_type_id"));
        entity.setStatusUpdate(rs.getLong("status_update"));
        entity.setCreateDeptCode(rs.getString("create_dept_code"));
        entity.setCreateDeptName(rs.getString("create_dept_name"));
        entity.setFollowDeptName(rs.getString("follow_dept_name"));
        entity.setNumberUrge(rs.getLong("number_urge"));
        entity.setTypeMission(rs.getLong("type_mission"));
        entity.setFollowName(rs.getString("follow_name"));
        entity.setContact(rs.getString("contact"));
        entity.setSynchronizedDate(rs.getDate("synchronized_date"));
        entity.setToIdentifyCode(rs.getString("to_identify_code"));
        entity.setToDeptName(rs.getString("to_dept_name"));
        entity.setSecurityTypeId(rs.getLong("security_type_id"));
        entity.setPublishAgencyName(rs.getString("publish_agency_name"));
        entity.setSignerName(rs.getString("signer_name"));
        entity.setPosName(rs.getString("pos_name"));
        entity.setTypeUrge(rs.getLong("type_urge"));
        entity.setStatusSys(rs.getLong("status_sys"));
        entity.setImportantTypeId(rs.getLong("important_type_id"));
        entity.setFollowIdentifyCode(rs.getString("follow_identify_code"));
        return entity;
    }
}
