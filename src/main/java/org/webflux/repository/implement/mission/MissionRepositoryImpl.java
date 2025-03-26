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
import org.webflux.domain.mission.MissionEntity;
import org.webflux.domain.mapper.mission.MissionEntityRowMapper;
import org.webflux.repository.mission.MissionRepository;
import org.webflux.service.dto.RoleUserDeptDTO;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MissionRepositoryImpl implements MissionRepository {
    private static final Logger log = LoggerFactory.getLogger(MissionRepositoryImpl.class);

    @Override
    public MissionEntity saveOrUpdate(JdbcTemplate jdbcTemplate, MissionEntity entity) {
        MissionEntity existingEntity = findById(jdbcTemplate, entity.getMissionId());
        if (Objects.nonNull(existingEntity)) {
            update(jdbcTemplate, entity);
        } else {
            insert(jdbcTemplate, entity);
        }
        return entity;
    }

    @Override
    public MissionEntity findById(JdbcTemplate jdbcTemplate, Long id) {
        String sql = "SELECT * FROM mission WHERE mission_id = ?";
        try {
            List<MissionEntity> lstResult = jdbcTemplate.query(sql,new Object[]{id} ,new MissionEntityRowMapper());
            if(!CollectionUtils.isEmpty(lstResult)) {
                return lstResult.get(0);
            }
        } catch (Exception ex) {
        }
        return  null;
    }

    @Override
    public MissionEntity findByCode(JdbcTemplate jdbcTemplate, String code) {
        String sql = "SELECT * FROM mission WHERE code = ?";
        try {
            List<MissionEntity> lstResults = jdbcTemplate.query(sql,new Object[]{code}, new MissionEntityRowMapper());
            if(!CollectionUtils.isEmpty(lstResults)) {
                return lstResults.get(0);
            }
        } catch (Exception ex) {
        }
        return null;
    }

    @Override
    public Optional<RoleUserDeptDTO> findByIdentifyCode(JdbcTemplate jdbcTemplate, String code) {
        String sql = """
                select
                  role.user_id, role.dept_id, role.role_id
                from
                  config_receive_task task
                  join department dep on dep.dept_id = task.config_dept_id
                  join role_user_dept role on role.role_user_dept_id = task.role_user_dept_id
                where
                  task.config_type = 0
                  and nvl( task.is_active, 1 ) = 1
                  and dep.identify_code = ?
                  and rownum = 1
        """;
        try {
            var roleUserDeptDTO = jdbcTemplate.queryForObject(sql, new Object[]{code}, new int[]{Types.VARCHAR},
            (rs, rowNum) -> {
                Long userId = rs.getLong("user_id");
                Long roleId = rs.getLong("role_id");
                Long deptId = rs.getLong("dept_id");
                return RoleUserDeptDTO.builder().userId(userId).roleId(roleId).deptId(deptId).build();
            });
            return Optional.of(roleUserDeptDTO);
        } catch (DataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public List<Long> findAllReceiverUserId(JdbcTemplate jdbcTemplate, String code) {
        String sql = """
                select
                  rdp.user_id
                from
                  config_receive_task task
                  join department dep on dep.dept_id = task.config_dept_id
                  join role_user_dept rdp on rdp.role_user_dept_id = task.role_user_dept_id
                  join  roles r on r.ROLE_ID = rdp.ROLE_ID
                where
                   r.STATUS = 1 and rdp.IS_ACTIVE = 1
                  and nvl( task.is_active, 1 ) = 1
                  and dep.identify_code = ?
                  order by r.ROLE_ORDER asc nulls last
        """;
        try {
            List<Long> results = jdbcTemplate.query(sql, new Object[]{code}, new int[]{Types.VARCHAR},
                    (rs, rowNum) -> {
                        Long userId = rs.getLong("user_id");
                        return userId;
                    });
            return results;
        } catch (DataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public Optional<String> findDeptPath(JdbcTemplate jdbcTemplate, String code) {
        String sql = """
                select
                  v.dept_path
                from
                  config_receive_task task
                  join department dep on dep.dept_id = task.config_dept_id
                  join role_user_dept role on role.role_user_dept_id = task.role_user_dept_id
                  join v_department v on dep.dept_id = v.dept_id
                where
                  task.config_type = 0
                  and nvl( task.is_active, 1 ) = 1
                  and dep.identify_code = ?
                  and rownum = 1
        """;
        try {
            var roleUserDeptDTO = jdbcTemplate.queryForObject(sql, new Object[]{code}, new int[]{Types.VARCHAR},
                    (rs, rowNum) -> rs.getString("dept_path"));
            return Optional.of(roleUserDeptDTO);
        } catch (DataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public int insert(JdbcTemplate jdbcTemplate, MissionEntity entity) {
        String sql = "INSERT INTO mission (mission_id, content, status, document_id, document_type, create_time, create_by, create_dept_id, create_role_id, deadline, finish_time, result, comments, document_code, abstract_document, is_edoc, code, date_publish, state, mission_type_id, status_update, create_dept_code, create_dept_name, follow_dept_name, number_urge, type_mission, follow_name, contact, synchronized_date, to_identify_code, to_dept_name, security_type_id, publish_agency_name, signer_name, pos_name, type_urge, status_sys, important_type_id, follow_identify_code) " +
                "VALUES (mission_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int result = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] {"mission_id"});
            ps.setString(1, entity.getContent());
            ps.setLong(2, entity.getStatus());
            ps.setLong(3, entity.getDocumentId());
            ps.setObject(4, entity.getDocumentType());
            ps.setDate(5, entity.getCreateTime());
            ps.setObject(6, entity.getCreateBy());
            ps.setObject(7, entity.getCreateDeptId());
            ps.setObject(8, entity.getCreateRoleId());
            ps.setObject(9, entity.getDeadline());
            ps.setObject(10, entity.getFinishTime());
            ps.setObject(11, entity.getResult());
            ps.setObject(12, entity.getComments());
            ps.setObject(13, entity.getDocumentCode());
            ps.setObject(14, entity.getAbstractDocument());
            ps.setObject(15, entity.getIsEdoc());
            ps.setObject(16, entity.getCode());
            ps.setObject(17, entity.getDatePublish());
            ps.setObject(18, entity.getState());
            ps.setObject(19, entity.getMissionTypeId());
            ps.setObject(20, entity.getStatusUpdate());
            ps.setObject(21, entity.getCreateDeptCode());
            ps.setObject(22, entity.getCreateDeptName());
            ps.setObject(23, entity.getFollowDeptName());
            ps.setObject(24, entity.getNumberUrge());
            ps.setObject(25, entity.getTypeMission());
            ps.setObject(26, entity.getFollowName());
            ps.setObject(27, entity.getContact());
            ps.setDate(28, entity.getSynchronizedDate());
            ps.setString(29, entity.getToIdentifyCode());
            ps.setString(30, entity.getToDeptName());
            ps.setObject(31, entity.getSecurityTypeId());
            ps.setString(32, entity.getPublishAgencyName());
            ps.setString(33, entity.getSignerName());
            ps.setString(34, entity.getPosName());
            ps.setObject(35, entity.getTypeUrge());
            ps.setObject(36, entity.getStatusSys());
            ps.setObject(37, entity.getImportantTypeId());
            ps.setString(38, entity.getFollowIdentifyCode());
            return ps;
        }, keyHolder);
        entity.setMissionId(keyHolder.getKey().longValue());
        return result;
    }

    public int update(JdbcTemplate jdbcTemplate, MissionEntity entity) {
        String sql = "UPDATE mission SET content = ?, status = ?, document_id = ?, document_type = ?, create_time = ?, create_by = ?, create_dept_id = ?, create_role_id = ?, deadline = ?, finish_time = ?, result = ?, comments = ?, document_code = ?, abstract_document = ?, is_edoc = ?, code = ?, date_publish = ?, state = ?, mission_type_id = ?, status_update = ?, create_dept_code = ?, create_dept_name = ?, follow_dept_name = ?, number_urge = ?, type_mission = ?, follow_name = ?, contact = ?, synchronized_date = ?, to_identify_code = ?, to_dept_name = ?, security_type_id = ?, publish_agency_name = ?, signer_name = ?, pos_name = ?, type_urge = ?, status_sys = ?, important_type_id = ?, follow_identify_code = ? " +
                "WHERE mission_id = ?";
        return jdbcTemplate.update(sql, entity.getContent(), entity.getStatus(), entity.getDocumentId(), entity.getDocumentType(), entity.getCreateTime(), entity.getCreateBy(), entity.getCreateDeptId(), entity.getCreateRoleId(), entity.getDeadline(), entity.getFinishTime(), entity.getResult(), entity.getComments(), entity.getDocumentCode(), entity.getAbstractDocument(), entity.getIsEdoc(), entity.getCode(), entity.getDatePublish(), entity.getState(), entity.getMissionTypeId(), entity.getStatusUpdate(), entity.getCreateDeptCode(), entity.getCreateDeptName(), entity.getFollowDeptName(), entity.getNumberUrge(), entity.getTypeMission(), entity.getFollowName(), entity.getContact(), entity.getSynchronizedDate(), entity.getToIdentifyCode(), entity.getToDeptName(), entity.getSecurityTypeId(), entity.getPublishAgencyName(), entity.getSignerName(), entity.getPosName(), entity.getTypeUrge(), entity.getStatusSys(), entity.getImportantTypeId(), entity.getFollowIdentifyCode(), entity.getMissionId());
    }
}
