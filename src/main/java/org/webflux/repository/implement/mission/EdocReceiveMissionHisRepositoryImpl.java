package org.webflux.repository.implement.mission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mission.EdocReceiveMissionHisEntity;
import org.webflux.domain.mapper.mission.EdocReceiveMissionHisEntityRowMapper;
import org.webflux.repository.mission.EdocReceiveMissionHisRepository;

import java.util.List;
import java.util.Objects;

@Repository
public class EdocReceiveMissionHisRepositoryImpl implements EdocReceiveMissionHisRepository {
    private static final Logger log = LoggerFactory.getLogger(EdocReceiveMissionHisRepositoryImpl.class);

    @Override
    public EdocReceiveMissionHisEntity findFirstByHistoryId(JdbcTemplate jdbcTemplate, Long historyId) {
        String sql = "SELECT * FROM edoc_receive_mission_his where history_id = ?";
        List<EdocReceiveMissionHisEntity> lstResults = jdbcTemplate.query(sql,new Object[]{historyId}, new EdocReceiveMissionHisEntityRowMapper());
        if(!lstResults.isEmpty()) {
            return lstResults.get(0);
        }
        return null;
    }

    @Override
    public void saveOrUpdate(JdbcTemplate jdbcTemplate, EdocReceiveMissionHisEntity entity) {
        if (Objects.nonNull(entity) && Objects.nonNull(entity.getEdocReceiveMissionHisId())) {
            updateEdocReceiveMissionHis(jdbcTemplate, entity);
        } else {
            saveEdocReceiveMissionHis(jdbcTemplate, entity);
        }
    }

    private void updateEdocReceiveMissionHis(JdbcTemplate jdbcTemplate, EdocReceiveMissionHisEntity entity) {
        String sql = "UPDATE edoc_receive_mission_his SET edoc_receive_mission_id = ?, history_id = ?, code = ?, type = ?, report_content = ?, status = ?, deadline_old = ?, deadline_new = ?, update_by_id = ?, update_by = ?, update_time = ?, to_identify_code = ?, to_dept_name = ? " +
                "WHERE edoc_receive_mission_his_id = ?";
        jdbcTemplate.update(sql, entity.getEdocReceiveMissionId(), entity.getHistoryId(), entity.getCode(), entity.getType(), entity.getReportContent(), entity.getStatus(), entity.getDeadlineOld(), entity.getDeadlineNew(), entity.getUpdateById(), entity.getUpdateBy(), entity.getUpdateTime(), entity.getToIdentifyCode(), entity.getToDeptName(), entity.getEdocReceiveMissionHisId());
    }

    private void saveEdocReceiveMissionHis(JdbcTemplate jdbcTemplate, EdocReceiveMissionHisEntity entity) {
        String sql = "INSERT INTO edoc_receive_mission_his (edoc_receive_mission_his_id, edoc_receive_mission_id, history_id, code, type, report_content, status, deadline_old, deadline_new, update_by_id, update_by, update_time, to_identify_code, to_dept_name) " +
                "VALUES (edoc_receive_mission_his_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, entity.getEdocReceiveMissionId(), entity.getHistoryId(), entity.getCode(), entity.getType(), entity.getReportContent(), entity.getStatus(), entity.getDeadlineOld(), entity.getDeadlineNew(), entity.getUpdateById(), entity.getUpdateBy(), entity.getUpdateTime(), entity.getToIdentifyCode(), entity.getToDeptName());
    }
}
