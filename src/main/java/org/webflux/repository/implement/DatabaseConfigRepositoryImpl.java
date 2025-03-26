package org.webflux.repository.implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.enumerate.ScriptQuery;
import org.webflux.repository.DatabaseConfigRepository;
import org.webflux.repository.query.DatabaseConfigQuery;

import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseConfigRepositoryImpl implements DatabaseConfigRepository {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<DatabaseConfigQuery> findById(Long id) {
        var databaseConfig = jdbcTemplate.queryForObject(ScriptQuery.getSingleDatabaseConfig, new Object[]{id},
                new int[]{Types.INTEGER},
                (rs, rowNum) -> DatabaseConfigQuery.builder()
                        .url(rs.getString("url"))
                        .port(rs.getInt("port"))
                        .service(rs.getString("service"))
                        .driver(rs.getString("driver"))
                        .password(rs.getString("password"))
                        .userName(rs.getString("user_name"))
                        .build());

        return Optional.ofNullable(databaseConfig);
    }

    public Optional<List<DatabaseConfigQuery>> findAll() {
        var databaseConfig = jdbcTemplate.query(ScriptQuery.getDatabaseConfig, (rs, rowNum) -> DatabaseConfigQuery.builder()
                .id(rs.getLong("id"))
                .url(rs.getString("url"))
                .port(rs.getInt("port"))
                .service(rs.getString("service"))
                .driver(rs.getString("driver"))
                .password(rs.getString("password"))
                .userName(rs.getString("user_name"))
                .name(rs.getString("name"))
                .build());

        return Optional.ofNullable(databaseConfig);
    }

    @Override
    public void optimizeDatabase() {

        // jdbcTemplate.execute("DELETE FROM your_table WHERE condition");

        // Optimize database settings
        jdbcTemplate.execute("PRAGMA auto_vacuum = FULL");
        jdbcTemplate.execute("PRAGMA cache_size = -2000");
        jdbcTemplate.execute("PRAGMA page_size = 4096");

        // Remove unnecessary indexes
        jdbcTemplate.execute("DROP INDEX IF EXISTS audit_log_index");
        jdbcTemplate.execute("DROP INDEX IF EXISTS sandbox_data_index");

        // Vacuum the database
        jdbcTemplate.execute("VACUUM");
    }
}
