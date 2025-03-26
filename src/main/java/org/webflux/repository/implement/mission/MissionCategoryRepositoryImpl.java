package org.webflux.repository.implement.mission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mapper.mission.CategoryEntityRowMapper;
import org.webflux.domain.mission.AttachsEntity;
import org.webflux.domain.mission.CategoryEntity;
import org.webflux.repository.mission.MissionCategoryRepository;

import java.sql.PreparedStatement;

@Repository
public class MissionCategoryRepositoryImpl implements MissionCategoryRepository {
    private static final Logger log = LoggerFactory.getLogger(MissionCategoryRepositoryImpl.class);

    @Override
    public CategoryEntity exist(JdbcTemplate jdbcTemplate) {
        String sql = "select * from category where category_type_code = 'MISSION_TYPE' and is_active = 1 and name = N'Nhiệm vụ CTCT' and rownum = 1";
        try {
            return jdbcTemplate.queryForObject(sql, new CategoryEntityRowMapper());
        } catch (DataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public CategoryEntity insert(JdbcTemplate jdbcTemplate, CategoryEntity entity) {
        String sql = "INSERT INTO category (category_id, name, description, is_active, dept_id, dept_name, code, category_type_code, value, sort_order, publish_code, exploitation_fee, note, unit, current_number) VALUES (category_seq.nextval,?,?,?,?,?,?,?,?,?,?,?,?,?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"category_id"});
            ps.setObject(1, entity.getName());
            ps.setObject(2, entity.getDescription());
            ps.setBoolean(3, entity.getIsActive());
            ps.setObject(4, entity.getDeptId());
            ps.setObject(5, entity.getDeptName());
            ps.setObject(6, entity.getCode());
            ps.setObject(7, entity.getCategoryTypeCode());
            ps.setObject(8, entity.getValue());
            ps.setObject(9, entity.getSortOrder());
            ps.setObject(10, entity.getPublishCode());
            ps.setObject(11, entity.getExploitationFee());
            ps.setObject(12, entity.getNote());
            ps.setObject(13, entity.getUnit());
            ps.setObject(14, entity.getCurrentNumber());
            return ps;
        }, keyHolder);

        entity.setCategoryId(keyHolder.getKey().longValue());
        return entity;
    }
}
