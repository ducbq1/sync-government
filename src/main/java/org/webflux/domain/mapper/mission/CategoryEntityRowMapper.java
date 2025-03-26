package org.webflux.domain.mapper.mission;

import org.springframework.jdbc.core.RowMapper;
import org.webflux.domain.mission.AttachsEntity;
import org.webflux.domain.mission.CategoryEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryEntityRowMapper implements RowMapper<CategoryEntity> {
    @Override
    public CategoryEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        CategoryEntity entity = CategoryEntity.builder().build();
        entity.setCategoryId(rs.getLong("category_id"));
        entity.setName(rs.getString("name"));
        entity.setDescription(rs.getString("description"));
        entity.setIsActive(rs.getBoolean("is_active"));
        entity.setDeptId(rs.getLong("category_id"));
        entity.setDeptName(rs.getString("dept_name"));
        entity.setCode(rs.getString("code"));
        entity.setCategoryTypeCode(rs.getString("category_type_code"));
        entity.setValue(rs.getLong("category_id"));
        entity.setSortOrder(rs.getLong("category_id"));
        entity.setPublishCode(rs.getString("publish_code"));
        entity.setExploitationFee(rs.getLong("category_id"));
        entity.setNote(rs.getString("note"));
        entity.setUnit(rs.getLong("unit"));
        entity.setCurrentNumber(rs.getLong("current_number"));

        return entity;
    }
}
