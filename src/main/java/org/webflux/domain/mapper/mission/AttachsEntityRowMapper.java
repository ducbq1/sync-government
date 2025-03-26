package org.webflux.domain.mapper.mission;

import org.springframework.jdbc.core.RowMapper;
import org.webflux.domain.mission.AttachsEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AttachsEntityRowMapper implements RowMapper<AttachsEntity> {
    @Override
    public AttachsEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AttachsEntity entity = new AttachsEntity();
        entity.setAttachId(rs.getLong("attach_id"));
        entity.setObjectId(rs.getLong("object_id"));
        entity.setObjectType(rs.getLong("object_type"));
        entity.setAttachName(rs.getString("attach_name"));
        entity.setAttachPath(rs.getString("attach_path"));
        entity.setIsActive(rs.getBoolean("is_active"));
        entity.setVersion(rs.getLong("version"));
        entity.setCreatorId(rs.getLong("creator_id"));
        entity.setModifierId(rs.getLong("modifier_id"));
        entity.setDateCreate(rs.getDate("date_create"));
        entity.setDateModify(rs.getDate("date_modify"));
        entity.setAttachType(rs.getLong("attach_type"));
        entity.setIsSigned(rs.getBoolean("is_signed"));
        entity.setIsPublished(rs.getBoolean("is_published"));
        entity.setModifierName(rs.getString("modifier_name"));
        entity.setSignerIds(rs.getString("signer_ids"));
        entity.setCloneAttachId(rs.getLong("clone_attach_id"));
        entity.setFlag(rs.getString("flag"));
        entity.setNoteId(rs.getString("note_id"));
        entity.setOldAttachPath(rs.getString("old_attach_path"));
        entity.setIsBackup(rs.getBoolean("is_backup"));
        entity.setIsComment(rs.getBoolean("is_comment"));
        entity.setSignerCa(rs.getString("signer_ca"));
        entity.setIsNote(rs.getBoolean("is_note"));
        entity.setSignerCaVersion(rs.getLong("signer_ca_version"));
        entity.setSavePath(rs.getString("save_path"));
        entity.setAttacksOrder(rs.getLong("attacks_order"));
        entity.setIsEncrypt(rs.getBoolean("is_encrypt"));
        entity.setOldDocId(rs.getLong("old_doc_id"));
        entity.setCaChecked(rs.getBoolean("ca_checked"));
        entity.setDraftType(rs.getLong("draft_type"));
        entity.setLastUpdate(rs.getDate("last_update"));
        entity.setIdFileDinhKem(rs.getLong("id_file_dinh_kem"));
        return entity;
    }
}
