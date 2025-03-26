package org.webflux.repository.implement.mission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.webflux.domain.mission.MissionDetailEntity;
import org.webflux.domain.mapper.mission.MissionDetailEntityRowMapper;
import org.webflux.repository.mission.MissionDetailRepository;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class MissionDetailRepositoryImpl implements MissionDetailRepository {
    private static final Logger log = LoggerFactory.getLogger(MissionDetailRepositoryImpl.class);

    @Override
    public MissionDetailEntity saveOrUpdate(JdbcTemplate jdbcTemplate, MissionDetailEntity entity) {
        MissionDetailEntity existingEntity = findById(jdbcTemplate, entity.getMissionDetailId());
        if (Objects.nonNull(existingEntity)) {
            update(jdbcTemplate, entity);
        } else {
            insert(jdbcTemplate, entity);
        }
        return entity;
    }

    @Override
    public MissionDetailEntity findById(JdbcTemplate jdbcTemplate, Long id) {
        String sql = "SELECT * FROM mission_detail WHERE mission_detail_id = ?";
        try {
            List<MissionDetailEntity> lstResult = jdbcTemplate.query(sql, new Object[]{id}, new MissionDetailEntityRowMapper());
            if(!CollectionUtils.isEmpty(lstResult)) {
                return lstResult.get(0);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    public List<MissionDetailEntity> findByCondition(JdbcTemplate jdbcTemplate, Map<String, Object> ...condition) {
        String sql = "SELECT * FROM mission_detail";

        if (Objects.nonNull(condition)) {
            sql.concat(" WHERE 1 = 1 ");
            if (condition.length > 1) {
                for (var cond : condition) {
                    if (cond instanceof HashMap<String, Object> con) {
                        sql.concat(" OR (1 = 1 ");
                        for (var entry : con.entrySet()) {
                            sql.concat(" AND " + entry.getKey() + " = " + entry.getValue());
                        }
                        sql.concat(")");
                    }
                }
            } else {
                for (var entry : condition[0].entrySet())
                sql.concat(" AND " + entry.getKey() + " = " + entry.getValue().toString());
            }
        }
        try {
            List<MissionDetailEntity> lstResult = jdbcTemplate.query(sql, new MissionDetailEntityRowMapper());
            return lstResult;
        } catch (DataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public List<MissionDetailEntity> findAll(JdbcTemplate jdbcTemplate) {
        String sql = "SELECT * FROM mission_detail";
        try {
            List<MissionDetailEntity> lstResult = jdbcTemplate.query(sql, new MissionDetailEntityRowMapper());
            return lstResult;
        } catch (DataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public List<MissionDetailEntity> findByCondition(JdbcTemplate jdbcTemplate, Predicate<MissionDetailEntity> condition) {

        String sql = "SELECT * FROM mission_detail";
        try {
            List<MissionDetailEntity> lstResult = jdbcTemplate.query(sql, new MissionDetailEntityRowMapper());
            return lstResult.stream().filter(condition).collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public MissionDetailEntity findByMissionId(JdbcTemplate jdbcTemplate, Long missionId) {
        String sql = "SELECT * FROM mission_detail WHERE mission_id = ?";
        try {
            List<MissionDetailEntity> lstResult = jdbcTemplate.query(sql, new Object[]{missionId}, new MissionDetailEntityRowMapper());
            if(!CollectionUtils.isEmpty(lstResult)) {
                return lstResult.get(0);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    public int insert(JdbcTemplate jdbcTemplate, MissionDetailEntity entity) {
        String sql = "INSERT INTO mission_detail (mission_detail_id, mission_id, mission_type, send_time, receiver_user_id, receiver_dept_id, receiver_role_id, state, is_active, is_read, finish_time, result, report_content, back_state, send_user_id, send_dept_id, send_role_id, parent_id, deadline, content, default_users_receiver, person_approval, is_task_master, is_dept, view_by, is_edoc, is_send, waiting_accept_deadline, next, back, node_id) " +
                "VALUES (mission_detail_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int result = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"mission_detail_id"});
            ps.setObject(1, entity.getMissionId());
            ps.setObject(2, entity.getMissionType());
            ps.setDate(3, entity.getSendTime());
            ps.setObject(4, entity.getReceiverUserId());
            ps.setObject(5, entity.getReceiverDeptId());
            ps.setObject(6, entity.getReceiverRoleId());
            ps.setObject(7, entity.getState());
            ps.setObject(8, entity.getIsActive());
            ps.setObject(9, entity.getIsRead());
            ps.setDate(10, entity.getFinishTime());
            ps.setObject(11, entity.getResult());
            ps.setString(12, entity.getReportContent());
            ps.setObject(13, entity.getBackState());
            ps.setObject(14, entity.getSendUserId());
            ps.setObject(15, entity.getSendDeptId());
            ps.setObject(16, entity.getSendRoleId());
            ps.setObject(17, entity.getParentId());
            ps.setDate(18, entity.getDeadline());
            ps.setString(19, entity.getContent());
            ps.setObject(20, entity.getDefaultUsersReceiver());
            ps.setObject(21, entity.getPersonApproval());
            ps.setObject(22, entity.getIsTaskMaster());
            ps.setObject(23, entity.getIsDept());
            ps.setObject(24, entity.getViewBy());
            ps.setObject(25, entity.getIsEdoc());
            ps.setObject(26, entity.getIsSend());
            ps.setObject(27, entity.getWaitingAcceptDeadline());
            ps.setObject(28, entity.getNext());
            ps.setObject(29, entity.getBack());
            ps.setObject(30, entity.getNodeId());
            return ps;
        }, keyHolder);
        entity.setMissionDetailId(keyHolder.getKey().longValue());
        return result;
    }

    public int update(JdbcTemplate jdbcTemplate, MissionDetailEntity entity) {
        String sql = "UPDATE mission_detail SET mission_id = ?, mission_type = ?, send_time = ?, receiver_user_id = ?, receiver_dept_id = ?, receiver_role_id = ?, state = ?, is_active = ?, is_read = ?, finish_time = ?, result = ?, report_content = ?, back_state = ?, send_user_id = ?, send_dept_id = ?, send_role_id = ?, parent_id = ?, deadline = ?, content = ?, default_users_receiver = ?, person_approval = ?, is_task_master = ?, is_dept = ?, view_by = ?, is_edoc = ?, is_send = ?, waiting_accept_deadline = ?, next = ?, back = ?, node_id = ? " +
                "WHERE mission_detail_id = ?";
        return jdbcTemplate.update(sql, entity.getMissionId(), entity.getMissionType(), entity.getSendTime(), entity.getReceiverUserId(), entity.getReceiverDeptId(), entity.getReceiverRoleId(), entity.getState(), entity.getIsActive(), entity.getIsRead(), entity.getFinishTime(), entity.getResult(), entity.getReportContent(), entity.getBackState(), entity.getSendUserId(), entity.getSendDeptId(), entity.getSendRoleId(), entity.getParentId(), entity.getDeadline(), entity.getContent(), entity.getDefaultUsersReceiver(), entity.getPersonApproval(), entity.getIsTaskMaster(), entity.getIsDept(), entity.getViewBy(), entity.getIsEdoc(), entity.getIsSend(), entity.getWaitingAcceptDeadline(), entity.getNext(), entity.getBack(), entity.getNodeId(), entity.getMissionDetailId());
    }
}
