package org.webflux.repository.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.webflux.domain.Category;
import org.webflux.enumerate.ScriptQuery;
import org.webflux.helper.StringUtils;
import org.webflux.model.StaticSourceResponse;
import org.webflux.repository.SourceRepository;
import org.webflux.service.dto.StaticFlowDTO;
import org.webflux.service.dto.StaticSourceDTO;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SourceRepositoryImpl implements SourceRepository {
    private JdbcTemplate jdbcTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public StaticSourceResponse findAll(int page, int pageSize) {
        // Write SQL
        String sqlQuery = String.format(ScriptQuery.getAllSource
                .concat(" limit ? offset ? ")
                .replaceAll("\\?", "%s"), pageSize, pageSize * (page - 1));

        // Get total source
        Integer total = jdbcTemplate.queryForObject(ScriptQuery.countAllSource, Integer.class);

        return getSourceResponse(total, sqlQuery);
    }

    @Override
    public StaticSourceResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize) {
        // Write SQL
        var sqlQuery = String.format(ScriptQuery.getAllSource
                        .concat("order by ? ? limit ? offset ?")
                        .replaceAll("\\?", "%s"), StringUtils.camelToSnake(sortColumn), StringUtils.camelToSnake(sortType),
                pageSize, pageSize * (page - 1));

        // Get total category
        var total = jdbcTemplate.queryForObject(ScriptQuery.countAllSource, Integer.class);

        // Return response for category
        return getSourceResponse(total, sqlQuery);
    }

    @Override
    public Optional<Category> findById(int id) {
        var category = jdbcTemplate.queryForObject(ScriptQuery.getSingleCategory, new Object[]{id},
                new int[]{Types.INTEGER},
                (rs, rowNum) -> Category.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .content(rs.getString("content"))
                        .token(rs.getString("token"))
                        .isActivated(rs.getBoolean("is_activated"))
                        .build()
        );

        return Optional.ofNullable(category);
    }

    @Override
    public void updateOne(Category category) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String currentDate = getCurrentSysdate();

        // Create new sync category entity
        Category categoryEntity = Category.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActivated(category.getIsActivated())
                .content(category.getContent())
                .token(category.getToken())
                .updatedAt(currentDate)
                .updatedBy(category.getUpdatedBy())
                .isDeleted(false)
                .type("API_BASE")
                .build();

        // Update database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(ScriptQuery.updateSource,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, categoryEntity.getName());
            ps.setString(2, categoryEntity.getType());
            ps.setString(3, categoryEntity.getContent());
            ps.setString(4, categoryEntity.getDescription());
            ps.setString(5, categoryEntity.getUpdatedAt());
            ps.setLong(6, categoryEntity.getUpdatedBy());
            ps.setInt(7, categoryEntity.getIsActivated() ? 1 : 0);
            ps.setString(8, categoryEntity.getType());
            ps.setString(9, categoryEntity.getToken());
            ps.setLong(10, categoryEntity.getId());
            return ps;
        }, keyHolder);
    }

    @Override
    public void updateMany(String sourceId, Boolean status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String currentDate = getCurrentSysdate();

        // Array of ids
        String[] listId = sourceId.split(",");

        // Join the types array with "', '" and wrap it with single quotes
        String listIdString = Arrays.stream(listId)
                .map(x -> "'" + x + "'")
                .collect(Collectors.joining(", "));

        // Write SQL
        String sqlQuery = ScriptQuery.updateSourceStatus.concat(" where id in (" + listIdString + ")");

        // Update data int database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sqlQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, status ? 1 : 0);
            ps.setLong(2, 1L);
            ps.setString(3, currentDate);
            return ps;
        }, keyHolder);
    }

    @Override
    public void saveEntity(Category category) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String currentDate = getCurrentSysdate();

        // Create new category entity
        Category categoryEntity = Category.builder()
                .name(category.getName())
                .description(category.getDescription())
                .isActivated(true)
                .content(category.getContent())
                .createdAt(currentDate)
                .createdBy(category.getCreatedBy())
                .isDeleted(false)
                .type("API_BASE")
                .build();

        // Insert into database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(ScriptQuery.insertSource,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, categoryEntity.getName());
            ps.setString(2, categoryEntity.getType());
            ps.setString(3, categoryEntity.getContent());
            ps.setString(4, categoryEntity.getDescription());
            ps.setLong(5, categoryEntity.getCreatedBy());
            ps.setString(6, categoryEntity.getCreatedAt());
            ps.setInt(7, categoryEntity.getIsActivated() ? 1 : 0);
            return ps;
        }, keyHolder);
    }

    @Override
    public StaticSourceResponse search(String name, String description, String content) {
        // Write SQL
        String sqlQuery = ScriptQuery.getAllSource;
        String countQuery = ScriptQuery.countAllSource;

        // Concat query
        if (Strings.isNotBlank(name)) {
            sqlQuery = sqlQuery.concat(" and name like '%?%'".replaceAll("\\?", name));
            countQuery = countQuery.concat(" and name like '%?%'".replaceAll("\\?", name));
        }

        if (Strings.isNotBlank(description)) {
            sqlQuery = sqlQuery.concat(" and description like '%?%'".replaceAll("\\?", description));
            countQuery = countQuery.concat(" and description like '%?%'".replaceAll("\\?", description));
        }

        if (Strings.isNotBlank(content)) {
            sqlQuery = sqlQuery.concat(" and content like '%?%'".replaceAll("\\?", content));
            countQuery = countQuery.concat(" and content like '%?%'".replaceAll("\\?", content));
        }

        sqlQuery = sqlQuery.concat(" limit ? offset ?")
                .replaceFirst("\\?", "10")
                .replaceAll("\\?", "0");

        // Get total static flow
        Integer total = jdbcTemplate.queryForObject(countQuery, Integer.class);

        // Return response for static source
        return getSourceResponse(total, sqlQuery);
    }

    public StaticSourceResponse getSourceResponse(Integer total, String sqlQuery) {
        // Get list of category
        var lstSource = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            try {
                return objectMapper.writeValueAsString(StaticSourceDTO.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .isActivated(rs.getBoolean("is_activated"))
                        .content(rs.getString("content"))
                        .type(rs.getString("type"))
                        .editButton(StaticFlowDTO.Button.builder()
                                .icon("<i class=\"fa-solid fa-pen-to-square\"></i>")
                                .callback("openEditModal")
                                .build())
                        .build());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });

        return StaticSourceResponse.builder()
                .total(Objects.nonNull(total) ? total : 0)
                .lstSource(lstSource)
                .build();
    }

    public String getCurrentSysdate() {
        return jdbcTemplate.queryForObject(ScriptQuery.getSystemDate, String.class);
    }
}