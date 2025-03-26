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
import org.webflux.domain.AuditLog;
import org.webflux.domain.Category;
import org.webflux.enumerate.ScriptQuery;
import org.webflux.helper.StringUtils;
import org.webflux.model.AuditLogResponse;
import org.webflux.model.CategoryResponse;
import org.webflux.model.StaticDestinationResponse;
import org.webflux.repository.CategoryRepository;
import org.webflux.repository.LogRepository;
import org.webflux.service.dto.AuditLogDTO;
import org.webflux.service.dto.CategoryDTO;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class LogRepositoryImpl implements LogRepository {
    private JdbcTemplate jdbcTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AuditLog> findAll() {
        String sqlQuery = ScriptQuery.getAllAuditLog;
        List<AuditLog> results = getCategoryByQuery(sqlQuery);
        return results;
    }

    @Override
    public AuditLogResponse findAll(int page, int pageSize) {
        // Write SQL
        String sqlQuery = String.format(ScriptQuery.getAllAuditLog
                .concat(" order by created_at desc")
                .concat(" limit ? offset ? ")
                .replaceAll("\\?", "%s"), pageSize, pageSize * (page - 1));

        // Get total source
        Integer total = jdbcTemplate.queryForObject(ScriptQuery.countAllAuditLog, Integer.class);

        return getAuditLogResponse(total, sqlQuery);
    }

    @Override
    public AuditLogResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize) {

        // Write SQL
        String sqlQuery = String.format(ScriptQuery.getAllAuditLog
                        .concat(" order by ? ? limit ? offset ?")
                        .replaceAll("\\?", "%s"), StringUtils.camelToSnake(sortColumn), StringUtils.camelToSnake(sortType),
                pageSize, pageSize * (page - 1));

        // Get total audit log
        Integer total = jdbcTemplate.queryForObject(ScriptQuery.countAllAuditLog, Integer.class);

        // Return response for audit log
        return getAuditLogResponse(total, sqlQuery);
    }

    @Override
    public Optional<AuditLog> findById(int id) {
        var auditLog = jdbcTemplate.queryForObject(ScriptQuery.getSingleAuditLog, new Object[]{id},
                new int[]{Types.INTEGER},
                (rs, rowNum) -> AuditLog.builder()
                        .id(rs.getLong("id"))
                        .createdBy(rs.getLong("created_by"))
                        .createdAt(rs.getString("created_at"))
                        .action(rs.getString("action"))
                        .content(rs.getString("content"))
                        .detail(rs.getString("detail"))
                        .isError(rs.getBoolean("is_error"))
                        .build());

        return Optional.ofNullable(auditLog);
    }

    @Override
    public AuditLogResponse search(String type, String action, String content, String detail, String createDateFrom, String createDateTo) {
        // Write SQL
        String sqlQuery = ScriptQuery.getAllAuditLog;
        String countQuery = ScriptQuery.countAllAuditLog;

        // Concat query
        if (Strings.isNotBlank(action)) {
            sqlQuery = sqlQuery.concat(" and action like '%?%'".replaceAll("\\?", action));
            countQuery = countQuery.concat(" and action like '%?%'".replaceAll("\\?", action));
        }

        if (Strings.isNotBlank(content)) {
            sqlQuery = sqlQuery.concat(" and content like '%?%'".replaceAll("\\?", content));
            countQuery = countQuery.concat(" and content like '%?%'".replaceAll("\\?", content));
        }

        if (Strings.isNotBlank(detail)) {
            sqlQuery = sqlQuery.concat(" and description like '%?%'".replaceAll("\\?", detail));
            countQuery = countQuery.concat(" and description like '%?%'".replaceAll("\\?", detail));
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
        return getAuditLogResponse(total, sqlQuery);
    }

    public void log(String action, String content, String detail, Long createBy) {
        log(action, content, detail, createBy, true);
    }

    public void log(String action, String content, String detail, Long createBy, Boolean isError) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sysDate = getCurrentSysdate();

        // Insert into database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(ScriptQuery.insertAuditLog,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, action);
            ps.setString(2, content);
            ps.setString(3, detail);
            ps.setLong(4, createBy);
            ps.setString(5, sysDate);
            ps.setLong(6, isError ? 1 : 0);
            return ps;
        }, keyHolder);
    }

    @Override
    public void log(AuditLog auditLog) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Insert into database
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(ScriptQuery.insertAuditLog,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, auditLog.getAction());
            ps.setString(2, auditLog.getContent());
            ps.setString(3, auditLog.getDetail());
            ps.setLong(4, auditLog.getCreatedBy());
            ps.setString(5, auditLog.getCreatedAt());
            ps.setLong(6, auditLog.getIsError() ? 1 : 0);
            return ps;
        }, keyHolder);
    }


    public AuditLogResponse getAuditLogResponse(Integer total, String sqlQuery) {
        List<AuditLog> lstAuditLog = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> AuditLog.builder()
                .id(rs.getLong("id"))
                .createdBy(rs.getLong("created_by"))
                .createdAt(rs.getString("created_at"))
                .action(rs.getString("action"))
                .content(rs.getString("content"))
                .detail(rs.getString("detail"))
                .isError(rs.getBoolean("is_error"))
                .build());

        // Convert to DTO
        List<String> lstAuditLogDTO = lstAuditLog.stream()
                .map(x -> {
                    try {
                        return objectMapper.writeValueAsString(AuditLogDTO.builder()
                                .id(x.getId())
                                .actionLog(x.getAction())
                                .content(x.getContent())
                                .detail(x.getDetail())
                                .isError(x.getIsError())
                                .createdAt(x.getCreatedAt())
                                .editButton(AuditLogDTO.Button.builder()
                                        .icon("<i class=\"fa-solid fa-pen-to-square\"></i>")
                                        .callback("openEditModal")
                                        .build())
                                .deleteButton(AuditLogDTO.Button.builder()
                                        .icon("<i class=\"fa-solid fa-trash\"></i>")
                                        .callback("openDeleteModal")
                                        .build())
                                .build());
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .toList();

        return AuditLogResponse.builder()
                .total(Objects.nonNull(total) ? total : 0)
                .lstAuditLog(lstAuditLogDTO)
                .build();
    }

    public List<AuditLog> getCategoryByQuery(String sqlQuery) {
        List<AuditLog> lstAuditLog = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> AuditLog.builder()
                .id(rs.getLong("id"))
                .createdBy(rs.getLong("created_by"))
                .createdAt(rs.getString("created_at"))
                .action(rs.getString("action"))
                .content(rs.getString("content"))
                .detail(rs.getString("detail"))
                .isError(rs.getBoolean("is_error"))
                .build());
        return lstAuditLog;
    }

    public String getCurrentSysdate() {
        return jdbcTemplate.queryForObject(ScriptQuery.getSystemDate, String.class);
    }
}
