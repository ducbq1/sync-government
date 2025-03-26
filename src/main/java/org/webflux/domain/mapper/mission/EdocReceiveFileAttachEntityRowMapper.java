package org.webflux.domain.mapper.mission;

import org.springframework.jdbc.core.RowMapper;
import org.webflux.domain.mission.EdocReceiveFileAttachEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EdocReceiveFileAttachEntityRowMapper implements RowMapper<EdocReceiveFileAttachEntity> {
    @Override
    public EdocReceiveFileAttachEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        EdocReceiveFileAttachEntity entity = new EdocReceiveFileAttachEntity();
        entity.setEdocReceiveFileAttachId(rs.getLong("edoc_receive_file_attach_id"));
        entity.setMissionId(rs.getLong("mission_id"));
        entity.setMissionHisId(rs.getLong("mission_his_id"));
        entity.setFileId(rs.getString("file_id"));
        entity.setTypeId(rs.getLong("type_id"));
        entity.setTenFile(rs.getString("ten_file"));
        entity.setIsSync(rs.getLong("is_sync"));
        entity.setFilePath(rs.getString("file_path"));
        return entity;
    }
}
