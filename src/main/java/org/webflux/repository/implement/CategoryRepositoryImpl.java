package org.webflux.repository.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.webflux.domain.Category;
import org.webflux.enumerate.ScriptQuery;
import org.webflux.helper.StringUtils;
import org.webflux.repository.query.DatabaseConfigQuery;
import org.webflux.service.dto.CategoryDTO;
import org.webflux.model.CategoryResponse;
import org.webflux.repository.CategoryRepository;
import java.lang.*;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.yaml.snakeyaml.nodes.Tag.STR;
import static org.yaml.snakeyaml.nodes.Tag.STR;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {
    private JdbcTemplate jdbcTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Category> findAll() {
        String sqlQuery = ScriptQuery.getAllCategory;
        List<Category> results = getCategoryByQuery(sqlQuery);
        return results;
    }

    @Override
    public CategoryResponse findAll(int page, int pageSize, String type) {
        String[] types = type.split(",");

        // Join the types array with "', '" and wrap it with single quotes
        String typesString = Arrays.stream(types)
                .map(x -> "'" + x + "'")
                .collect(Collectors.joining(", "));

        // Write SQL
        String sqlQuery = String.format(ScriptQuery.getAllCategory
                .concat(" where type in (" + typesString + ")")
                .concat(" limit ? offset ? ")
                .replaceAll("\\?", "%s"), pageSize, pageSize * (page - 1));

        // Get total category
        Integer total = jdbcTemplate.queryForObject(ScriptQuery.countAllCategory
                .concat(" where type in (" + typesString + ")"), Integer.class);

        return getCategoryResponse(total, sqlQuery);
    }

    @Override
    public CategoryResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize, String type) {
        String[] types = type.split(",");

        // Join the types array with "', '" and wrap it with single quotes
        String typesString = Arrays.stream(types)
                .map(x -> "'" + x + "'")
                .collect(Collectors.joining(", "));

        // Write SQL
        String sqlQuery = String.format(ScriptQuery.getAllCategory
                .concat(" where type in (" + typesString + ")")
                .concat("order by ? ? limit ? offset ?")
                .replaceAll("\\?", "%s"), StringUtils.camelToSnake(sortColumn), StringUtils.camelToSnake(sortType),
                pageSize, pageSize * (page - 1));

        // Get total category
        Integer total = jdbcTemplate.queryForObject(ScriptQuery.countAllCategory
                .concat(" where type in (" + typesString + ")"), Integer.class);

        // Return response for category
        return getCategoryResponse(total, sqlQuery);
    }

    @Override
    public Optional<Category> findById(int id) {
        var category = jdbcTemplate.queryForObject(ScriptQuery.getSingleCategory, new Object[] { id },
                new int[] { Types.INTEGER },
                (rs, rowNum) -> Category.builder()
                        .id(rs.getLong("id"))
                        .createdBy(rs.getLong("created_by"))
                        .createdAt(rs.getString("created_at"))
                        .updatedAt(rs.getString("updated_at"))
                        .updatedBy(rs.getLong("updated_by"))
                        .isDeleted(rs.getBoolean("is_deleted"))
                        .isActivated(rs.getBoolean("is_activated"))
                        .name(rs.getString("name"))
                        .type(rs.getString("type"))
                        .content(rs.getString("content"))
                        .description(rs.getString("description"))
                        .databaseConfigId(rs.getLong("database_config_id"))
                        .build());

        return Optional.ofNullable(category);
    }

    @Override
    public CategoryResponse search(String type, String name, String content, String description, String createDateFrom,
            String createDateTo) {
        String[] types = type.split(",");

        // Join the types array with "', '" and wrap it with single quotes
        String typesString = Arrays.stream(types)
                .map(x -> "'" + x + "'")
                .collect(Collectors.joining(", "));

        // Write SQL
        String sqlQuery = ScriptQuery.getAllCategory
                .concat(" where type in (" + typesString + ")");
        String countQuery = ScriptQuery.countAllCategory
                .concat(" where type in (" + typesString + ")");

        // Concat query
        if (Strings.isNotBlank(name)) {
            sqlQuery = sqlQuery.concat(" and name like '%?%'".replaceAll("\\?", name));
            countQuery = countQuery.concat(" and name like '%?%'".replaceAll("\\?", name));
        }

        if (Strings.isNotBlank(content)) {
            sqlQuery = sqlQuery.concat(" and content like '%?%'".replaceAll("\\?", content));
            countQuery = countQuery.concat(" and content like '%?%'".replaceAll("\\?", content));
        }

        if (Strings.isNotBlank(description)) {
            sqlQuery = sqlQuery.concat(" and description like '%?%'".replaceAll("\\?", description));
            countQuery = countQuery.concat(" and description like '%?%'".replaceAll("\\?", description));
        }

        if (Strings.isNotBlank(createDateFrom)) {
            sqlQuery = sqlQuery.concat(" and created_at >= '?'".replaceAll("\\?", createDateFrom));
            countQuery = countQuery.concat(" and created_at >= '?'".replaceAll("\\?", createDateFrom));
        }

        if (Strings.isNotBlank(createDateTo)) {
            sqlQuery = sqlQuery.concat(" and created_at <= '?'".replaceAll("\\?", createDateTo));
            countQuery = countQuery.concat(" and created_at <= '?'".replaceAll("\\?", createDateTo));
        }

        sqlQuery = sqlQuery.concat(" limit ? offset ?")
                .replaceFirst("\\?", "10")
                .replaceAll("\\?", "0");

        // Get total category
        Integer total = jdbcTemplate.queryForObject(countQuery, Integer.class);

        // Return response for category
        return getCategoryResponse(total, sqlQuery);
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sysDate = getCurrentSysdate();

        // Create new category entity
        Category categoryEntity = Category.builder()
                .id(categoryDTO.getId())
                .name(categoryDTO.getName())
                .type(categoryDTO.getType())
                .description(categoryDTO.getDescription())
                .content(categoryDTO.getContent())
                .isActivated(categoryDTO.getIsActivated())
                .isDeleted(categoryDTO.getIsDeleted())
                .updatedAt(sysDate)
                .updatedBy(1L)
                .databaseConfigId(null)
                .build();

        // Update database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(ScriptQuery.updateCategory,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, categoryEntity.getName());
            ps.setString(2, categoryEntity.getType());
            ps.setString(3, categoryEntity.getContent());
            ps.setString(4, categoryEntity.getDescription());
            ps.setString(5, categoryEntity.getUpdatedAt());
            ps.setLong(6, categoryEntity.getUpdatedBy());
            ps.setInt(7, categoryEntity.getIsActivated() ? 1 : 0);
            ps.setInt(8, categoryEntity.getIsDeleted() ? 1 : 0);
            ps.setLong(9, categoryEntity.getId());
            return ps;
        }, keyHolder);
    }

    @Override
    public void delete(Long id) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sysDate = getCurrentSysdate();

        // Insert into database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(ScriptQuery.deleteCategory,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, "1");
            ps.setString(2, sysDate);
            ps.setLong(3, id);
            return ps;
        }, keyHolder);
    }

    @Override
    public void deleteMany(String ids) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sysDate = getCurrentSysdate();

        // Array of ids
        String[] listId = ids.split(",");

        // Join the types array with "', '" and wrap it with single quotes
        String listIdString = Arrays.stream(listId)
                .map(x -> "'" + x + "'")
                .collect(Collectors.joining(", "));

        // Write SQL
        String sqlQuery = ScriptQuery.deleteMultipleCategories.concat(" where id in (" + listIdString + ")");

        // Insert into database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sqlQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, "1");
            ps.setString(2, sysDate);
            return ps;
        }, keyHolder);
    }

    @Override
    public void saveCategory(CategoryDTO categoryDTO) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sysDate = getCurrentSysdate();

        // Create new category entity
        Category categoryEntity = Category.builder()
                .name(categoryDTO.getName())
                .type(categoryDTO.getType())
                .description(categoryDTO.getDescription())
                .content(categoryDTO.getContent())
                .isActivated(categoryDTO.getIsActivated())
                .isDeleted(categoryDTO.getIsDeleted())
                .createdAt(sysDate)
                .updatedAt(sysDate)
                .createdBy(1L)
                .updatedBy(1L)
                .databaseConfigId(null)
                .build();

        // Insert into database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(ScriptQuery.insertCategory,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, categoryEntity.getName());
            ps.setString(2, categoryEntity.getType());
            ps.setString(3, categoryEntity.getContent());
            ps.setString(4, categoryEntity.getDescription());
            ps.setLong(5, categoryEntity.getUpdatedBy());
            ps.setString(6, categoryEntity.getUpdatedAt());
            ps.setLong(7, categoryEntity.getCreatedBy());
            ps.setString(8, categoryEntity.getCreatedAt());
            ps.setString(9, categoryEntity.getIsDeleted() ? "1" : "0");
            ps.setString(10, categoryEntity.getIsActivated() ? "1" : "0");
            ps.setString(11, String.valueOf(categoryEntity.getDatabaseConfigId()));
            return ps;
        }, keyHolder);
    }

    @Override
    public List<Category> findAllByName(List<String> lstName) {
        String namesString = lstName.stream()
                .map(x -> "'" + x + "'")
                .collect(Collectors.joining(", "));
        String sqlQuery = ScriptQuery.getAllCategory
                .concat(" where name in (" + namesString + ")");
        return getCategoryByQuery(sqlQuery);
    }

    @Override
    public List<Category> findAllByType(List<String> lstType) {
        String typesString = lstType.stream()
                .map(x -> "'" + x + "'")
                .collect(Collectors.joining(", "));
        String sqlQuery = ScriptQuery.getAllCategory
                .concat(" where type in (" + typesString + ")")
                .concat( "order by name desc");
        return getCategoryByQuery(sqlQuery);
    }

    @Override
    public Category findById(Long id) {
        String sqlQuery = ScriptQuery.getAllCategory
                .concat(" where id = " + id);
        List<Category> results = getCategoryByQuery(sqlQuery);
        if (!CollectionUtils.isEmpty(results)) {
            return results.get(0);
        }
        return null;
    }

    public CategoryResponse getCategoryResponse(Integer total, String sqlQuery) {
        // Get list of category
        List<Category> lstCategory = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            return Category.builder()
                    .id(rs.getLong("id"))
                    .createdBy(rs.getLong("created_by"))
                    .createdAt(rs.getString("created_at"))
                    .updatedAt(rs.getString("updated_at"))
                    .updatedBy(rs.getLong("updated_by"))
                    .isDeleted(rs.getBoolean("is_deleted"))
                    .isActivated(rs.getBoolean("is_activated"))
                    .name(rs.getString("name"))
                    .type(rs.getString("type"))
                    .content(rs.getString("content"))
                    .description(rs.getString("description"))
                    .databaseConfigId(rs.getLong("database_config_id"))
                    .build();
        });

        // Convert to DTO
        List<String> lstCategoryDTO = lstCategory.stream()
                .map(x -> {
                    try {
                        return objectMapper.writeValueAsString(CategoryDTO.builder()
                                .id(x.getId())
                                .name(x.getName())
                                .type(x.getType())
                                .content(x.getContent())
                                .isActivated(x.getIsActivated())
                                .isDeleted(x.getIsDeleted())
                                .description(x.getDescription())
                                .updatedBy(x.getUpdatedBy())
                                .updatedAt(x.getUpdatedAt())
                                .createdBy(x.getCreatedBy())
                                .createdAt(x.getCreatedAt())
                                .databaseConfigId(x.getDatabaseConfigId())
                                .editButton(CategoryDTO.Button.builder()
                                        .icon("<i class=\"fa-solid fa-pen-to-square\"></i>")
                                        .callback("openEditModal")
                                        .build())
                                .deleteButton(CategoryDTO.Button.builder()
                                        .icon("<i class=\"fa-solid fa-trash\"></i>")
                                        .callback("openDeleteModal")
                                        .build())
                                .build());
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .toList();

        return CategoryResponse.builder()
                .total(Objects.nonNull(total) ? total : 0)
                .lstCategory(lstCategoryDTO)
                .build();
    }

    public List<Category> getCategoryByQuery(String sqlQuery) {
        // Get list of category
        List<Category> lstCategory = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> Category.builder()
                .id(rs.getLong("id"))
                .createdBy(rs.getLong("created_by"))
                .createdAt(rs.getString("created_at"))
                .updatedAt(rs.getString("updated_at"))
                .updatedBy(rs.getLong("updated_by"))
                .isDeleted(rs.getBoolean("is_deleted"))
                .isActivated(rs.getBoolean("is_activated"))
                .name(rs.getString("name"))
                .type(rs.getString("type"))
                .content(rs.getString("content"))
                .description(rs.getString("description"))
                .databaseConfigId(rs.getLong("database_config_id"))
                .payload(rs.getString("payload"))
                .token(rs.getString("token"))
                .sequence(rs.getString("sequence"))
                .uniqueKey(rs.getString("unique_key"))
                .primaryKey(rs.getString("primary_key"))
                .build());
        return lstCategory;
    }

    public String getCurrentSysdate() {
        return jdbcTemplate.queryForObject(ScriptQuery.getSystemDate, String.class);
    }
}
