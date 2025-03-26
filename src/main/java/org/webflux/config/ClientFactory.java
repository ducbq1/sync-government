package org.webflux.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.webflux.helper.DatabaseUtils;

import javax.sql.DataSource;
import java.util.Objects;

@Log4j2
@Component
public class ClientFactory {

    private JdbcTemplate jdbcTemplate;
    private static JdbcTemplate instance;

    @Autowired
    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static DataSource getDataSource(String driver, String url, String username, String password) {
        DataSourceBuilder dsBuilder = DataSourceBuilder.create();
        dsBuilder.driverClassName(driver);
        dsBuilder.url(url);
        dsBuilder.username(username);
        dsBuilder.password(password);
        return dsBuilder.build();
    }

    public JdbcTemplate getJdbcTemplate(String driver, String url, String username, String password) {
        try {
            if (Objects.isNull(instance) || instance.getDataSource() != getDataSource(driver, url, username, password)) {
                instance = new JdbcTemplate(getDataSource(driver, url, username, password));
            }
            return instance;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    public static JdbcTemplate getJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
