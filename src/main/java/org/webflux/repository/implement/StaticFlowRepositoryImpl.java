package org.webflux.repository.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.webflux.domain.SyncFlowStatic;
import org.webflux.enumerate.ScriptQuery;
import org.webflux.helper.StringUtils;
import org.webflux.model.StaticFlowResponse;
import org.webflux.repository.StaticFlowRepository;
import org.webflux.repository.query.SyncFlowStaticQuery;
import org.webflux.service.dto.DestinationConfigDTO;
import org.webflux.service.dto.SourceConfigDTO;
import org.webflux.service.dto.StaticFlowDTO;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class StaticFlowRepositoryImpl implements StaticFlowRepository {
    private JdbcTemplate jdbcTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SyncFlowStaticQuery> findAll() {
        try {
            return jdbcTemplate.query(ScriptQuery.getAllSyncFlowStaticConfig, (rs, rowNum) -> SyncFlowStaticQuery.builder()
                    .id(rs.getLong("id"))
                    .content(rs.getString("content"))
                    .token(rs.getString("token"))
                    .url(rs.getString("url"))
                    .port(rs.getInt("port"))
                    .userName(rs.getString("user_name"))
                    .password(rs.getString("password"))
                    .driver(rs.getString("driver"))
                    .service(rs.getString("service"))
                    .payload(rs.getString("payload"))
                    .proxy(rs.getString("proxy"))
                    .saveFilePath(rs.getString("save_file_path"))
                    .isGetSyncedAgain(rs.getBoolean("is_get_synced_again"))
                    .build());
        } catch (DataAccessException ex) {
            return null;
        }
    }

    @Override
    public StaticFlowResponse findAll(int page, int pageSize) {
        // Write SQL
        String sqlQuery = String.format(ScriptQuery.getAllSyncFlowStatic
                .concat(" limit ? offset ? ")
                .replaceAll("\\?", "%s"), pageSize, pageSize * (page - 1));

        // Get total static flow
        Integer total = jdbcTemplate.queryForObject(ScriptQuery.countAllSyncFlowStatic, Integer.class);

        return getFlowResponse(total, sqlQuery);
    }

    @Override
    public StaticFlowResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize) {
        // Write SQL
        var sqlQuery = String.format(ScriptQuery.getAllSyncFlowStatic
                        .concat("order by ? ? limit ? offset ?")
                        .replaceAll("\\?", "%s"), StringUtils.camelToSnake(sortColumn), StringUtils.camelToSnake(sortType),
                pageSize, pageSize * (page - 1));

        // Get total static flow
        var total = jdbcTemplate.queryForObject(ScriptQuery.countAllSyncFlowStatic, Integer.class);

        // Return response for static flow
        return getFlowResponse(total, sqlQuery);
    }

    @Override
    public Optional<SyncFlowStatic> findById(int id) {
        var syncFlow = jdbcTemplate.queryForObject(ScriptQuery.getSingleSyncFlowStatic, new Object[]{id},
                new int[]{Types.INTEGER},
                (rs, rowNum) -> SyncFlowStatic.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .sourceId(rs.getLong("source_id"))
                        .destinationId(rs.getLong("destination_id"))
                        .isActivated(rs.getBoolean("is_activated"))
                        .payload(rs.getString("payload"))
                        .proxy(rs.getString("proxy"))
                        .saveFilePath(rs.getString("save_file_path"))
                        .isGetSyncedAgain(rs.getBoolean("is_get_synced_again"))
                        .build()
        );

        return Optional.ofNullable(syncFlow);
    }

    @Override
    public StaticFlowResponse search(String name, String description, String sourceName, String destinationName) {
        // Write SQL
        String sqlQuery = ScriptQuery.getAllSyncFlowStatic.concat("where 1 = 1 and ");
        String countQuery = ScriptQuery.countAllSyncFlowStatic.concat("where 1 = 1 and ");

        // Concat query
        if (Strings.isNotBlank(name)) {
            sqlQuery = sqlQuery.concat(" and a.name like '%?%'".replaceAll("\\?", name));
            countQuery = countQuery.concat(" and a.name like '%?%'".replaceAll("\\?", name));
        }

        if (Strings.isNotBlank(description)) {
            sqlQuery = sqlQuery.concat(" and a.description like '%?%'".replaceAll("\\?", description));
            countQuery = countQuery.concat(" and a.description like '%?%'".replaceAll("\\?", description));
        }

        if (Strings.isNotBlank(sourceName)) {
            sqlQuery = sqlQuery.concat(" and source_name like '%?%'".replaceAll("\\?", sourceName));
            countQuery = countQuery.concat(" and b.name like '%?%'".replaceAll("\\?", sourceName));
        }

        if (Strings.isNotBlank(destinationName)) {
            sqlQuery = sqlQuery.concat(" and destination_name like '%?%'".replaceAll("\\?", destinationName));
            countQuery = countQuery.concat(" and c.name like '%?%'".replaceAll("\\?", destinationName));
        }

        sqlQuery = sqlQuery.concat(" limit ? offset ?")
                .replaceFirst("and", "")
                .replaceFirst("\\?", "10")
                .replaceAll("\\?", "0");

        countQuery = countQuery.replaceFirst("and", "");

        // Get total static flow
        Integer total = jdbcTemplate.queryForObject(countQuery, Integer.class);

        // Return response for static flow
        return getFlowResponse(total, sqlQuery);
    }

    @Override
    public int updateOne(SyncFlowStatic syncFlowStatic) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String currentDate = getCurrentSysdate();

        // Create new sync flow static entity
        SyncFlowStatic syncFlowEntity = SyncFlowStatic.builder()
                .id(syncFlowStatic.getId())
                .name(syncFlowStatic.getName())
                .description(syncFlowStatic.getDescription())
                .isActivated(syncFlowStatic.getIsActivated())
                .sourceId(syncFlowStatic.getSourceId())
                .destinationId(syncFlowStatic.getDestinationId())
                .updatedAt(currentDate)
                .updatedBy(syncFlowStatic.getUpdatedBy())
                .payload(syncFlowStatic.getPayload())
                .proxy(syncFlowStatic.getProxy())
                .saveFilePath(syncFlowStatic.getSaveFilePath())
                .isGetSyncedAgain(syncFlowStatic.getIsGetSyncedAgain())
                .build();

        // Update database
        return jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(ScriptQuery.updateSyncFlowStatic,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, syncFlowEntity.getName());
            ps.setString(2, syncFlowEntity.getDescription());
            ps.setInt(3, syncFlowStatic.getIsActivated() ? 1 : 0);
            ps.setLong(4, syncFlowEntity.getSourceId());
            ps.setLong(5, syncFlowEntity.getDestinationId());
            ps.setString(6, syncFlowEntity.getUpdatedAt());
            ps.setLong(7, syncFlowEntity.getUpdatedBy());
            ps.setString(8, syncFlowEntity.getProxy());
            ps.setString(9, syncFlowEntity.getPayload());
            ps.setString(10, syncFlowEntity.getSaveFilePath());
            ps.setBoolean(11, syncFlowEntity.getIsGetSyncedAgain());
            ps.setLong(12, syncFlowEntity.getId());
            return ps;
        }, keyHolder);
    }

    @Override
    public void updateMany(String flowId, Boolean status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String currentDate = getCurrentSysdate();

        // Array of ids
        String[] listId = flowId.split(",");

        // Join the types array with "', '" and wrap it with single quotes
        String listIdString = Arrays.stream(listId)
                .map(x -> "'" + x + "'")
                .collect(Collectors.joining(", "));

        // Write SQL
        String sqlQuery = ScriptQuery.updateFlowStatus.concat(" where id in (" + listIdString + ")");

        // Update data int database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sqlQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, status ? 1 : 0);
            ps.setString(2, "1");
            ps.setString(3, currentDate);
            return ps;
        }, keyHolder);
    }

    @Override
    public void saveFlow(SyncFlowStatic syncFlowStatic) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String currentDate = getCurrentSysdate();

        // Create new sync flow static entity
        SyncFlowStatic syncFlowEntity = SyncFlowStatic.builder()
                .name(syncFlowStatic.getName())
                .description(syncFlowStatic.getDescription())
                .isActivated(syncFlowStatic.getIsActivated())
                .sourceId(syncFlowStatic.getSourceId())
                .destinationId(syncFlowStatic.getDestinationId())
                .createdBy(syncFlowStatic.getCreatedBy())
                .createdAt(currentDate)
                .payload(syncFlowStatic.getPayload())
                .proxy(syncFlowStatic.getProxy())
                .saveFilePath(syncFlowStatic.getSaveFilePath())
                .isGetSyncedAgain(syncFlowStatic.getIsGetSyncedAgain())
                .build();

        // Insert into database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(ScriptQuery.insertSyncFlowStatic,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, syncFlowEntity.getName());
            ps.setString(2, syncFlowEntity.getDescription());
            ps.setInt(3, syncFlowEntity.getIsActivated() ? 1 : 0);
            ps.setLong(4, syncFlowEntity.getSourceId());
            ps.setLong(5, syncFlowEntity.getDestinationId());
            ps.setString(6, syncFlowEntity.getProxy());
            ps.setString(7, syncFlowEntity.getPayload());
            ps.setString(9, syncFlowEntity.getSaveFilePath());
            ps.setBoolean(10, syncFlowEntity.getIsGetSyncedAgain());
            return ps;
        }, keyHolder);
    }

    public StaticFlowResponse getFlowResponse(Integer total, String sqlQuery) {
        // Get list of static flow
        var lstSettingDTO = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            try {
                return objectMapper.writeValueAsString(StaticFlowDTO.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .sourceId(rs.getLong("source_id"))
                        .destinationId(rs.getLong("destination_id"))
                        .isActivated(rs.getBoolean("is_activated"))
                        .sourceName(rs.getString("source_name"))
                        .destinationName(rs.getString("destination_name"))
                        .payload(rs.getString("payload"))
                        .proxy(rs.getString("proxy"))
                        .saveFilePath(rs.getString("save_file_path"))
                        .editButton(StaticFlowDTO.Button.builder()
                                .icon("<i class=\"fa-solid fa-pen-to-square\"></i>")
                                .callback("openEditModal")
                                .build())
                        .build());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });

        var sourceConfig = jdbcTemplate.query(ScriptQuery.getSourceConfig, (rs, rowNum) -> {
            try {
                return objectMapper.writeValueAsString(SourceConfigDTO.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });

        var databaseConfig = jdbcTemplate.query(ScriptQuery.getDestinationConfig, (rs, rowNum) -> {
            try {
                return objectMapper.writeValueAsString(DestinationConfigDTO.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });

        return StaticFlowResponse.builder()
                .total(Objects.nonNull(total) ? total : 0)
                .lstFlow(lstSettingDTO)
                .lstSourceConfig(sourceConfig)
                .lstDestinationConfig(databaseConfig)
                .build();
    }

    public String getCurrentSysdate() {
        return jdbcTemplate.queryForObject(ScriptQuery.getSystemDate, String.class);
    }
}