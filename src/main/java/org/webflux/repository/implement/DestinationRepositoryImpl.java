package org.webflux.repository.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.webflux.domain.DatabaseConfig;
import org.webflux.enumerate.ScriptQuery;
import org.webflux.helper.StringUtils;
import org.webflux.model.StaticDestinationResponse;
import org.webflux.repository.DestinationRepository;
import org.webflux.service.dto.StaticDestinationDTO;
import org.webflux.service.dto.StaticFlowDTO;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class DestinationRepositoryImpl implements DestinationRepository {
    private JdbcTemplate jdbcTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public StaticDestinationResponse findAll(int page, int pageSize) {
        // Write SQL
        String sqlQuery = String.format(ScriptQuery.getAllDestination
                .concat(" limit ? offset ? ")
                .replaceAll("\\?", "%s"), pageSize, pageSize * (page - 1));

        // Get total source
        Integer total = jdbcTemplate.queryForObject(ScriptQuery.countAllDestination, Integer.class);

        return getDestinationResponse(total, sqlQuery);
    }

    @Override
    public StaticDestinationResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize) {
        // Write SQL
        var sqlQuery = String.format(ScriptQuery.getAllDestination
                        .concat("order by ? ? limit ? offset ?")
                        .replaceAll("\\?", "%s"), StringUtils.camelToSnake(sortColumn), StringUtils.camelToSnake(sortType),
                pageSize, pageSize * (page - 1));

        // Get total category
        var total = jdbcTemplate.queryForObject(ScriptQuery.countAllDestination, Integer.class);

        // Return response for category
        return getDestinationResponse(total, sqlQuery);
    }

    @Override
    public Optional<DatabaseConfig> findById(int id) {
        var databaseConfig = jdbcTemplate.queryForObject(ScriptQuery.getSingleDatabaseConfig, new Object[]{id},
                new int[]{Types.INTEGER},
                (rs, rowNum) -> DatabaseConfig.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .url(rs.getString("url"))
                        .userName(rs.getString("user_name"))
                        .password(rs.getString("password"))
                        .driver(rs.getString("driver"))
                        .port(rs.getInt("port"))
                        .service(rs.getString("service"))
                        .isConnected(rs.getBoolean("is_connected"))
                        .isActivated(rs.getBoolean("is_activated"))
                        .build()
        );

        return Optional.ofNullable(databaseConfig);
    }

    @Override
    public void updateOne(DatabaseConfig databaseConfig) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String currentDate = getCurrentSysdate();

        // Create new sync config entity
        DatabaseConfig databaseConfigEntity = DatabaseConfig.builder()
                .id(databaseConfig.getId())
                .name(databaseConfig.getName())
                .description(databaseConfig.getDescription())
                .url(databaseConfig.getUrl())
                .userName(databaseConfig.getUserName())
                .password(databaseConfig.getPassword())
                .driver(databaseConfig.getDriver())
                .port(databaseConfig.getPort())
                .service(databaseConfig.getService())
                .isActivated(databaseConfig.getIsActivated())
                .updatedAt(currentDate)
                .updatedBy(databaseConfig.getUpdatedBy())
                .isDeleted(false)
                .build();

        // Update database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(ScriptQuery.updateDatabaseConfig,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, databaseConfigEntity.getName());
            ps.setString(2, databaseConfigEntity.getDescription());
            ps.setString(3, databaseConfigEntity.getUpdatedAt());
            ps.setLong(4, databaseConfigEntity.getUpdatedBy());
            ps.setInt(5, databaseConfigEntity.getIsActivated() ? 1 : 0);
            ps.setString(6, databaseConfigEntity.getUrl());
            ps.setString(7, databaseConfigEntity.getUserName());
            ps.setString(8, databaseConfigEntity.getPassword());
            ps.setString(9, databaseConfigEntity.getDriver());
            ps.setInt(10, databaseConfigEntity.getPort());
            ps.setString(11, databaseConfigEntity.getService());
            ps.setLong(12, databaseConfigEntity.getId());
            return ps;
        }, keyHolder);
    }

    @Override
    public void updateMany(String configId, Boolean status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String currentDate = getCurrentSysdate();

        // Array of ids
        String[] listId = configId.split(",");

        // Join the types array with "', '" and wrap it with single quotes
        String listIdString = Arrays.stream(listId)
                .map(x -> "'" + x + "'")
                .collect(Collectors.joining(", "));

        // Write SQL
        String sqlQuery = ScriptQuery.updateConfigStatus.concat(" where id in (" + listIdString + ")");

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
    public void updateConnection(int configID, long updatedBy, boolean isConnected) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String currentDate = getCurrentSysdate();

        // Update database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(ScriptQuery.updateDatabaseConfigConnection,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, currentDate);
            ps.setLong(2, updatedBy);
            ps.setBoolean(3, isConnected);
            ps.setInt(4, configID);
            return ps;
        }, keyHolder);
    }

    @Override
    public void saveEntity(DatabaseConfig databaseConfig) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String currentDate = getCurrentSysdate();

        // Create new database config entity
        DatabaseConfig databaseConfigEntity = DatabaseConfig.builder()
                .name(databaseConfig.getName())
                .description(databaseConfig.getDescription())
                .url(databaseConfig.getUrl())
                .userName(databaseConfig.getUserName())
                .password(databaseConfig.getPassword())
                .driver(databaseConfig.getDriver())
                .port(databaseConfig.getPort())
                .service(databaseConfig.getService())
                .createdAt(currentDate)
                .createdBy(databaseConfig.getCreatedBy())
                .isActivated(databaseConfig.getIsActivated())
                .isConnected(databaseConfig.getIsConnected())
                .isDeleted(databaseConfig.getIsDeleted())
                .build();

        // Insert into database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(ScriptQuery.insertDatabaseConfig,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, databaseConfigEntity.getName());
            ps.setString(2, databaseConfigEntity.getDescription());
            ps.setString(3, databaseConfigEntity.getUrl());
            ps.setString(4, databaseConfigEntity.getUserName());
            ps.setString(5, databaseConfigEntity.getPassword());
            ps.setString(6, databaseConfigEntity.getDriver());
            ps.setInt(7, databaseConfigEntity.getPort());
            ps.setString(8, databaseConfigEntity.getService());
            ps.setLong(9, databaseConfigEntity.getCreatedBy());
            ps.setString(10, databaseConfigEntity.getCreatedAt());
            ps.setInt(11, databaseConfigEntity.getIsConnected() ? 1 : 0);
            ps.setInt(12, databaseConfigEntity.getIsActivated() ? 1 : 0);
            ps.setInt(13, databaseConfigEntity.getIsDeleted() ? 1 : 0);
            return ps;
        }, keyHolder);
    }

    @Override
    public StaticDestinationResponse search(String name, String description, String url) {
        // Write SQL
        String sqlQuery = ScriptQuery.getAllDestination.concat("where 1 = 1 and ");
        String countQuery = ScriptQuery.countAllDestination.concat("where 1 = 1 and ");

        // Concat query
        if (Strings.isNotBlank(name)) {
            sqlQuery = sqlQuery.concat(" and name like '%?%'".replaceAll("\\?", name));
            countQuery = countQuery.concat(" and name like '%?%'".replaceAll("\\?", name));
        }

        if (Strings.isNotBlank(description)) {
            sqlQuery = sqlQuery.concat(" and description like '%?%'".replaceAll("\\?", description));
            countQuery = countQuery.concat(" and description like '%?%'".replaceAll("\\?", description));
        }

        if (Strings.isNotBlank(url)) {
            sqlQuery = sqlQuery.concat(" and url like '%?%'".replaceAll("\\?", url));
            countQuery = countQuery.concat(" and url like '%?%'".replaceAll("\\?", url));
        }

        sqlQuery = sqlQuery.concat(" limit ? offset ?")
                .replaceFirst("and", "")
                .replaceFirst("\\?", "10")
                .replaceAll("\\?", "0");

        countQuery = countQuery.replaceFirst("and", "");

        // Get total static flow
        Integer total = jdbcTemplate.queryForObject(countQuery, Integer.class);

        // Return response for static source
        return getDestinationResponse(total, sqlQuery);
    }

    public StaticDestinationResponse getDestinationResponse(Integer total, String sqlQuery) {
        // Get list of category
        var lstConfig = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            try {
                return objectMapper.writeValueAsString(StaticDestinationDTO.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .url(rs.getString("url"))
                        .isActivated(rs.getBoolean("is_activated"))
                        .isConnected(rs.getBoolean("is_connected"))
                        .editButton(StaticFlowDTO.Button.builder()
                                .icon("<i class=\"fa-solid fa-pen-to-square\"></i>")
                                .callback("openEditModal")
                                .build())
                        .checkButton(StaticFlowDTO.Button.builder()
                                .icon("<i class=\"fa-solid fa-wifi\"></i>")
                                .callback("checkConnection")
                                .build())
                        .build());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });

        return StaticDestinationResponse.builder()
                .total(Objects.nonNull(total) ? total : 0)
                .lstConfig(lstConfig)
                .build();
    }

    public String getCurrentSysdate() {
        return jdbcTemplate.queryForObject(ScriptQuery.getSystemDate, String.class);
    }
}