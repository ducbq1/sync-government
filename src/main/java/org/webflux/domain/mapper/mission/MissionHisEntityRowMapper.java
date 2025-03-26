package org.webflux.domain.mapper.mission;

import org.springframework.jdbc.core.RowMapper;
import org.webflux.domain.mission.MissionHisEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MissionHisEntityRowMapper implements RowMapper<MissionHisEntity> {

    @Override
    public MissionHisEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        MissionHisEntity entity = new MissionHisEntity();
        entity.setMissionHisId(rs.getLong("mission_his_id"));
        entity.setMissionId(rs.getLong("mission_id"));
        entity.setUserId(rs.getLong("user_id"));
        entity.setActionType(rs.getLong("action_type"));
        entity.setContent(rs.getString("content"));
        entity.setCreateTime(rs.getDate("create_time"));
        entity.setReceiverId(rs.getLong("receiver_id"));
        entity.setReceiverType(rs.getLong("receiver_type"));
        entity.setDefaultUserRec(rs.getLong("default_user_rec"));
        entity.setDetailId(rs.getLong("detail_id"));
        entity.setReportInfo(rs.getString("report_info"));
        entity.setFileAttachs(rs.getString("file_attachs"));
        entity.setCode(rs.getString("code"));
        entity.setType(rs.getLong("type"));
        entity.setReportContent(rs.getString("report_content"));
        entity.setStatus(rs.getLong("status"));
        entity.setDeadlineOld(rs.getDate("deadline_old"));
        entity.setDeadlineNew(rs.getDate("deadline_new"));
        entity.setUpdateById(rs.getLong("update_by_id"));
        entity.setUpdateBy(rs.getString("update_by"));
        entity.setUpdateTime(rs.getDate("update_time"));
        entity.setToIdentifyCode(rs.getString("to_identify_code"));
        entity.setToDeptName(rs.getString("to_dept_name"));
        entity.setDocumentId(rs.getLong("document_id"));
        entity.setDocumentCode(rs.getString("document_code"));
        entity.setAbstractDocument(rs.getString("abstract_document"));
        entity.setIsEdoc(rs.getString("is_edoc"));
        return entity;
    }
}
