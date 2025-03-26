package org.webflux.repository.implement.mission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.webflux.domain.mission.EdocReceiveFileAttachEntity;
import org.webflux.domain.mapper.mission.EdocReceiveFileAttachEntityRowMapper;
import org.webflux.repository.mission.EdocReceiveFileAttachRepository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Repository
public class EdocReceiveFileAttachRepositoryImpl implements EdocReceiveFileAttachRepository {
    private static final Logger log = LoggerFactory.getLogger(EdocReceiveFileAttachRepositoryImpl.class);

    @Override
    public EdocReceiveFileAttachEntity saveOrUpdate(JdbcTemplate jdbcTemplate, EdocReceiveFileAttachEntity entity) {
        if (Objects.nonNull(entity) && Objects.nonNull(entity.getEdocReceiveFileAttachId())) {
            update(jdbcTemplate, entity);
        } else {
            insert(jdbcTemplate, entity);
        }
        return entity;
    }

    @Override
    public List<EdocReceiveFileAttachEntity> findByIsSync(JdbcTemplate jdbcTemplate, Long isSync, Long maxRow) {
        String sql = "SELECT * FROM edoc_receive_file_attach where is_sync = ? and rownum < ?";
        List<EdocReceiveFileAttachEntity> lstResults = jdbcTemplate.query(sql, new Object[]{isSync, maxRow}, new EdocReceiveFileAttachEntityRowMapper());
        return lstResults;
    }

    @Override
    public EdocReceiveFileAttachEntity findByFileIdAndMissionId(JdbcTemplate jdbcTemplate, Long fileId, Long missionId) {
        String sql = "SELECT * FROM edoc_receive_file_attach where file_id = ? and mission_id = ?";
        List<EdocReceiveFileAttachEntity> lstResults = jdbcTemplate.query(sql, new Object[]{fileId, missionId}, new EdocReceiveFileAttachEntityRowMapper());
        if(!CollectionUtils.isEmpty(lstResults)) {
            return lstResults.get(0);
        }
        return null;
    }

    private EdocReceiveFileAttachEntity insert(JdbcTemplate jdbcTemplate, EdocReceiveFileAttachEntity entity) {
        String sql = "INSERT INTO edoc_receive_file_attach (edoc_receive_file_attach_id, mission_id, mission_his_id, file_id, type_id, ten_file, is_sync, file_path) VALUES (edoc_receive_file_attach_seq.nextval, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"edoc_receive_file_attach_id"});
                ps.setObject(1, entity.getMissionId());
                ps.setObject(2, entity.getMissionHisId());
                ps.setString(3, entity.getFileId());
                ps.setObject(4, entity.getTypeId());
                ps.setString(5, entity.getTenFile());
                ps.setObject(6, entity.getIsSync());
                ps.setString(7, entity.getFilePath());
                return ps;

        }, keyHolder);

        entity.setEdocReceiveFileAttachId(keyHolder.getKey().longValue());
        return entity;
    }

    private EdocReceiveFileAttachEntity update(JdbcTemplate jdbcTemplate, EdocReceiveFileAttachEntity entity) {
        String sql = "UPDATE edoc_receive_file_attach SET mission_id = ?, mission_his_id = ?, file_id = ?, type_id = ?, ten_file = ?, is_sync = ?, file_path = ? WHERE edoc_receive_file_attach_id = ?";
        jdbcTemplate.update(sql,
                entity.getMissionId(),
                entity.getMissionHisId(),
                entity.getFileId(),
                entity.getTypeId(),
                entity.getTenFile(),
                entity.getIsSync(),
                entity.getFilePath(),
                entity.getEdocReceiveFileAttachId());
        return entity;
    }
}
