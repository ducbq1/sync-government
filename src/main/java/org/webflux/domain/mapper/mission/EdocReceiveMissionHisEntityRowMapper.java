package org.webflux.domain.mapper.mission;

import org.springframework.jdbc.core.RowMapper;
import org.webflux.domain.mission.EdocReceiveMissionHisEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EdocReceiveMissionHisEntityRowMapper implements RowMapper<EdocReceiveMissionHisEntity> {
    @Override
    public EdocReceiveMissionHisEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        EdocReceiveMissionHisEntity entity = new EdocReceiveMissionHisEntity();
        entity.setEdocReceiveMissionHisId(rs.getLong("edoc_receive_mission_his_id"));
        entity.setEdocReceiveMissionId(rs.getLong("edoc_receive_mission_id"));
        entity.setHistoryId(rs.getLong("history_id"));
        entity.setCode(rs.getString("code"));
        entity.setType(rs.getLong("type"));
        entity.setReportContent(rs.getString("report_content"));
        entity.setStatus(rs.getLong("status"));
        entity.setDeadlineOld(rs.getTimestamp("deadline_old"));
        entity.setDeadlineNew(rs.getTimestamp("deadline_new"));
        entity.setUpdateById(rs.getLong("update_by_id"));
        entity.setUpdateBy(rs.getString("update_by"));
        entity.setUpdateTime(rs.getTimestamp("update_time"));
        entity.setToIdentifyCode(rs.getString("to_identify_code"));
        entity.setToDeptName(rs.getString("to_dept_name"));
        return entity;
    }
}
