package org.webflux.domain.mapper.mission;

import org.springframework.jdbc.core.RowMapper;
import org.webflux.domain.mission.EdocMissionCombineEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EdocMissionCombineEntityRowMapper implements RowMapper<EdocMissionCombineEntity> {
    @Override
    public EdocMissionCombineEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        EdocMissionCombineEntity entity = new EdocMissionCombineEntity();
        entity.setMissionCombineId(rs.getLong("mission_combine_id"));
        entity.setEdocReceiveMissionId(rs.getLong("EDOC_RECEIVE_MISSION_ID"));
        entity.setMissionId(rs.getLong("MISSION_ID"));
        entity.setCode(rs.getString("code"));
        entity.setCombineCode(rs.getString("combine_code"));
        entity.setCombineName(rs.getString("combine_name"));
        entity.setType(rs.getLong("type"));
        return entity;
    }
}
