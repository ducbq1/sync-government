package org.webflux.repository.implement.mission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mission.EdocMissionCombineEntity;
import org.webflux.domain.mapper.mission.EdocMissionCombineEntityRowMapper;
import org.webflux.repository.mission.EdocMissionCombineRepository;

import java.util.List;
import java.util.Objects;

@Repository
public class EdocMissionCombineRepositoryImpl implements EdocMissionCombineRepository {
    private static final Logger log = LoggerFactory.getLogger(EdocMissionCombineRepositoryImpl.class);

    @Override
    public EdocMissionCombineEntity findFirstByEdocReceiveMissionIdCodeCombineCode(JdbcTemplate jdbcTemplate, Long edocReceiveMissionId, String code, String combineCode, String combineName) {
        String sql = "SELECT * FROM edoc_mission_combine where EDOC_RECEIVE_MISSION_ID = ? and CODE = ? and (COMBINE_CODE = ? OR COMBINE_CODE IS NULL AND COMBINE_NAME = ?)";
        List<EdocMissionCombineEntity> lstResults = jdbcTemplate.query(sql,new Object[]{edocReceiveMissionId, code, combineCode, combineName}, new EdocMissionCombineEntityRowMapper());
        if(!lstResults.isEmpty()) {
            return  lstResults.get(0);
        }
        return null;
    }

    @Override
    public void saveOrUpdate(JdbcTemplate jdbcTemplate, EdocMissionCombineEntity entity) {
        if (Objects.nonNull(entity) && Objects.nonNull(entity.getMissionCombineId())) {
            update(jdbcTemplate, entity);
        } else {
            insert(jdbcTemplate, entity);
        }
    }

    public int insert(JdbcTemplate jdbcTemplate, EdocMissionCombineEntity entity) {
        String sql = "INSERT INTO edoc_mission_combine (mission_combine_id, EDOC_RECEIVE_MISSION_ID, code, combine_code, combine_name, type, mission_id) VALUES (mission_combine_seq.nextval, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, entity.getEdocReceiveMissionId(), entity.getCode(), entity.getCombineCode(), entity.getCombineName(), entity.getType(), entity.getMissionId());
    }

    public int update(JdbcTemplate jdbcTemplate, EdocMissionCombineEntity entity) {
        String sql = "UPDATE edoc_mission_combine SET EDOC_RECEIVE_MISSION_ID = ?, code = ?, combine_code = ?, combine_name = ?, type = ?, mission_id = ? WHERE mission_combine_id = ?";
        return jdbcTemplate.update(sql, entity.getEdocReceiveMissionId(), entity.getCode(), entity.getCombineCode(), entity.getCombineName(), entity.getType(), entity.getMissionId() ,entity.getMissionCombineId());
    }
}
