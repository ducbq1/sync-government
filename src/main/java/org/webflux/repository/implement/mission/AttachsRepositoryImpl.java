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
import org.webflux.domain.mission.AttachsEntity;
import org.webflux.domain.mapper.mission.AttachsEntityRowMapper;
import org.webflux.repository.mission.AttachsRepository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Repository
public class AttachsRepositoryImpl implements AttachsRepository {
    private static final Logger log = LoggerFactory.getLogger(AttachsRepositoryImpl.class);

    @Override
    public AttachsEntity saveOrUpdate(JdbcTemplate jdbcTemplate, AttachsEntity entity) {
        if (Objects.nonNull(entity) && Objects.nonNull(entity.getAttachId())) {
             update(jdbcTemplate, entity);
        } else {
            insert(jdbcTemplate, entity);
        }
        return entity;
    }

    @Override
    public AttachsEntity findByFileIdAndObjectId(JdbcTemplate jdbcTemplate, Long fileId, Long objectId) {
        String sql = "SELECT * FROM attachs where id_file_dinh_kem = ? and object_id = ?";
        List<AttachsEntity> lstResults = jdbcTemplate.query(sql, new Object[]{fileId, objectId}, new AttachsEntityRowMapper());
        if(!CollectionUtils.isEmpty(lstResults)) {
            return lstResults.get(0);
        }
        return null;
    }

    private AttachsEntity insert(JdbcTemplate jdbcTemplate, AttachsEntity entity) {
        String sql = "INSERT INTO attachs (attach_id, object_id, object_type, attach_name, attach_path, is_active, version, creator_id, modifier_id, date_create, date_modify, attach_type, is_signed, is_published, modifier_name, signer_ids, clone_attach_id, flag, note_id, old_attach_path, is_backup, is_comment, signer_ca, is_note, signer_ca_version, save_path, attacks_order, is_encrypt, old_doc_id, ca_checked, draft_type, last_update, id_file_dinh_kem) VALUES (attachs_seq.nextval,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"attach_id"});
                ps.setObject(1, entity.getObjectId());
                ps.setObject(2, entity.getObjectType());
                ps.setString(3, entity.getAttachName());
                ps.setString(4, entity.getAttachPath());
                ps.setObject(5, entity.getIsActive());
                ps.setObject(6, entity.getVersion());
                ps.setObject(7, entity.getCreatorId());
                ps.setObject(8, entity.getModifierId());
                ps.setDate(9, entity.getDateCreate());
                ps.setDate(10, entity.getDateModify());
                ps.setObject(11, entity.getAttachType());
                ps.setObject(12, entity.getIsSigned());
                ps.setObject(13, entity.getIsPublished());
                ps.setString(14, entity.getModifierName());
                ps.setString(15, entity.getSignerIds());
                ps.setObject(16, entity.getCloneAttachId());
                ps.setString(17, entity.getFlag());
                ps.setString(18, entity.getNoteId());
                ps.setString(19, entity.getOldAttachPath());
                ps.setObject(20, entity.getIsBackup());
                ps.setObject(21, entity.getIsComment());
                ps.setString(22, entity.getSignerCa());
                ps.setObject(23, entity.getIsNote());
                ps.setObject(24, entity.getSignerCaVersion());
                ps.setString(25, entity.getSavePath());
                ps.setObject(26, entity.getAttacksOrder());
                ps.setObject(27, entity.getIsEncrypt());
                ps.setObject(28, entity.getOldDocId());
                ps.setObject(29, entity.getCaChecked());
                ps.setObject(30, entity.getDraftType());
                ps.setDate(31, entity.getLastUpdate());
                ps.setObject(32, entity.getIdFileDinhKem());
                return ps;
            }, keyHolder);

        entity.setAttachId(keyHolder.getKey().longValue());
        return entity;
    }

    private AttachsEntity update(JdbcTemplate jdbcTemplate, AttachsEntity entity) {
        String sql = "UPDATE attachs SET object_id = ?, object_type = ?, attach_name = ?, attach_path = ?, is_active = ?, version = ?, creator_id = ?, modifier_id = ?, date_create = ?, date_modify = ?, attach_type = ?, is_signed = ?, is_published = ?, modifier_name = ?, signer_ids = ?, clone_attach_id = ?, flag = ?, note_id = ?, old_attach_path = ?, is_backup = ?, is_comment = ?, signer_ca = ?, is_note = ?, signer_ca_version = ?, save_path = ?, attacks_order = ?, is_encrypt = ?, old_doc_id = ?, ca_checked = ?, draft_type = ?, last_update = ?, id_file_dinh_kem = ? WHERE attach_id = ?";

        jdbcTemplate.update(sql,
                entity.getObjectId(),
                entity.getObjectType(),
                entity.getAttachName(),
                entity.getAttachPath(),
                entity.getIsActive(),
                entity.getVersion(),
                entity.getCreatorId(),
                entity.getModifierId(),
                entity.getDateCreate(),
                entity.getDateModify(),
                entity.getAttachType(),
                entity.getIsSigned(),
                entity.getIsPublished(),
                entity.getModifierName(),
                entity.getSignerIds(),
                entity.getCloneAttachId(),
                entity.getFlag(),
                entity.getNoteId(),
                entity.getOldAttachPath(),
                entity.getIsBackup(),
                entity.getIsComment(),
                entity.getSignerCa(),
                entity.getIsNote(),
                entity.getSignerCaVersion(),
                entity.getSavePath(),
                entity.getAttacksOrder(),
                entity.getIsEncrypt(),
                entity.getOldDocId(),
                entity.getCaChecked(),
                entity.getDraftType(),
                entity.getLastUpdate(),
                entity.getIdFileDinhKem(),
                entity.getAttachId());
        return entity;
    }
}
