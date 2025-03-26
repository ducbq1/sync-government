package org.webflux.repository.implement.mission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mission.EdocReceiveMissionEntity;
import org.webflux.domain.mapper.mission.EdocReceiveMissionEntityRowMapper;
import org.webflux.repository.mission.EdocReceiveMissionRepository;

import java.sql.Types;
import java.util.List;
import java.util.Objects;

@Repository
public class EdocReceiveMissionRepositoryImpl implements EdocReceiveMissionRepository {
    private static final Logger log = LoggerFactory.getLogger(EdocReceiveMissionRepositoryImpl.class);

    @Override
    public EdocReceiveMissionEntity findFirstByCode(JdbcTemplate jdbcTemplate, String code) {
        String sql = "SELECT * FROM edoc_receive_mission where code = ? and rownum = 1";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{code}, new EdocReceiveMissionEntityRowMapper());
        } catch (DataAccessException ex) {
            return null;
        }
    }

    @Override
    public void saveOrUpdate(JdbcTemplate jdbcTemplate, EdocReceiveMissionEntity entity) {
        if (Objects.nonNull(entity) && Objects.nonNull(entity.getEdocReceiveMissionId())) {
            updateEdocReceiveMission(jdbcTemplate, entity);
        } else {
            saveEdocReceiveMission(jdbcTemplate, entity);
        }
    }

    @Override
    public List<EdocReceiveMissionEntity> findAllByStatusSys(JdbcTemplate jdbcTemplate, Long statusSys) {
        String sql = "SELECT * FROM edoc_receive_mission where status_Sys = ?";
        List<EdocReceiveMissionEntity> lstResults = jdbcTemplate.query(sql, new Object[]{statusSys}, new int[]{Types.INTEGER}, new EdocReceiveMissionEntityRowMapper());
        return lstResults;
    }

    private void updateEdocReceiveMission(JdbcTemplate jdbcTemplate, EdocReceiveMissionEntity entity) {
        String updateSql = "UPDATE edoc_receive_mission SET identify_from_code = ?, identify_from_name = ?, code = ?, document_type = ?, status_sys = ?, status_update = ?, date_publish = ?, content = ?, ctct = ?, document_code = ?, create_dept_code = ?, create_dept_name = ?, create_by = ?, create_time = ?, follow_identify_code = ?, follow_dept_name = ?, mission_type = ?, deadline = ?, number_urge = ?, type_mission = ?, follow_name = ?, contact = ?, synchronized_date = ?, to_identify_code = ?, to_dept_name = ?, is_send = ?, scope = ?, document_id = ?, abstract = ?, publish_agency_name = ?, date_create = ?, security_type_id = ?, signer_name = ?, pos_name = ?, edxml = ? WHERE edoc_receive_mission_id = ?";

        jdbcTemplate.update(updateSql, entity.getIdentifyFromCode(), entity.getIdentifyFromName(), entity.getCode(), entity.getDocumentType(), entity.getStatusSys(), entity.getStatusUpdate(), entity.getDatePublish(), entity.getContent(), entity.getCtct(), entity.getDocumentCode(), entity.getCreateDeptCode(), entity.getCreateDeptName(), entity.getCreateBy(), entity.getCreateTime(), entity.getFollowIdentifyCode(), entity.getFollowDeptName(), entity.getMissionType(), entity.getDeadline(), entity.getNumberUrge(), entity.getTypeMission(), entity.getFollowName(), entity.getContact(), entity.getSynchronizedDate(), entity.getToIdentifyCode(), entity.getToDeptName(), entity.getIsSend(), entity.getScope(), entity.getDocumentId(), entity.getAbstractContent(), entity.getPublishAgencyName(), entity.getDateCreate(), entity.getSecurityTypeId(), entity.getSignerName(), entity.getPosName(), entity.getEdxml(), entity.getEdocReceiveMissionId());
    }

    private void saveEdocReceiveMission(JdbcTemplate jdbcTemplate, EdocReceiveMissionEntity entity) {
        String insertSql = "INSERT INTO edoc_receive_mission (edoc_receive_mission_id, identify_from_code, identify_from_name, code, document_type, status_sys, status_update, date_publish, content, ctct, document_code, create_dept_code, create_dept_name, create_by, create_time, follow_identify_code, follow_dept_name, mission_type, deadline, number_urge, type_mission, follow_name, contact, synchronized_date, to_identify_code, to_dept_name, is_send, scope, document_id, abstract, publish_agency_name, date_create, security_type_id, signer_name, pos_name, edxml) VALUES (edoc_receive_mission_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(insertSql, entity.getIdentifyFromCode(), entity.getIdentifyFromName(), entity.getCode(), entity.getDocumentType(), entity.getStatusSys(), entity.getStatusUpdate(), entity.getDatePublish(), entity.getContent(), entity.getCtct(), entity.getDocumentCode(), entity.getCreateDeptCode(), entity.getCreateDeptName(), entity.getCreateBy(), entity.getCreateTime(), entity.getFollowIdentifyCode(), entity.getFollowDeptName(), entity.getMissionType(), entity.getDeadline(), entity.getNumberUrge(), entity.getTypeMission(), entity.getFollowName(), entity.getContact(), entity.getSynchronizedDate(), entity.getToIdentifyCode(), entity.getToDeptName(), entity.getIsSend(), entity.getScope(), entity.getDocumentId(), entity.getAbstractContent(), entity.getPublishAgencyName(), entity.getDateCreate(), entity.getSecurityTypeId(), entity.getSignerName(), entity.getPosName(), entity.getEdxml());
    }
}
