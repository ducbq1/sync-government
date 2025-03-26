package org.webflux.domain.mapper.mission;

import org.springframework.jdbc.core.RowMapper;
import org.webflux.domain.mission.MissionDetailEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MissionDetailEntityRowMapper implements RowMapper<MissionDetailEntity> {

    @Override
    public MissionDetailEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        MissionDetailEntity entity = new MissionDetailEntity();
        entity.setMissionDetailId(rs.getLong("mission_detail_id"));
        entity.setMissionId(rs.getLong("mission_id"));
        entity.setMissionType(rs.getLong("mission_type"));
        entity.setSendTime(rs.getDate("send_time"));
        entity.setReceiverUserId(rs.getLong("receiver_user_id"));
        entity.setReceiverDeptId(rs.getLong("receiver_dept_id"));
        entity.setReceiverRoleId(rs.getLong("receiver_role_id"));
        entity.setState(rs.getLong("state"));
        entity.setIsActive(rs.getBoolean("is_active"));
        entity.setIsRead(rs.getBoolean("is_read"));
        entity.setFinishTime(rs.getDate("finish_time"));
        entity.setResult(rs.getLong("result"));
        entity.setReportContent(rs.getString("report_content"));
        entity.setBackState(rs.getLong("back_state"));
        entity.setSendUserId(rs.getLong("send_user_id"));
        entity.setSendDeptId(rs.getLong("send_dept_id"));
        entity.setSendRoleId(rs.getLong("send_role_id"));
        entity.setParentId(rs.getLong("parent_id"));
        entity.setDeadline(rs.getDate("deadline"));
        entity.setContent(rs.getString("content"));
        entity.setDefaultUsersReceiver(rs.getString("default_users_receiver"));
        entity.setPersonApproval(rs.getLong("person_approval"));
        entity.setIsTaskMaster(rs.getLong("is_task_master"));
        entity.setIsDept(rs.getLong("is_dept"));
        entity.setViewBy(rs.getLong("view_by"));
        entity.setIsEdoc(rs.getBoolean("is_edoc"));
        entity.setIsSend(rs.getBoolean("is_send"));
        entity.setWaitingAcceptDeadline(rs.getBoolean("waiting_accept_deadline"));
        entity.setNext(rs.getLong("next"));
        entity.setBack(rs.getLong("back"));
        entity.setNodeId(rs.getInt("node_id"));
        return entity;
    }
}
