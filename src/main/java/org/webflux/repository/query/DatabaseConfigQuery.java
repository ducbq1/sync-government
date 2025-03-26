package org.webflux.repository.query;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.parameters.P;

@Data
@Builder
public class DatabaseConfigQuery {
    private Long id;
    private String url;
    private String userName;
    private String databaseName;
    private String name;
    private String instance;
    private String password;
    private String driver;
    private Integer port;
    private String service;

    public String getUrl() {
        if ("oracle.jdbc.OracleDriver".equals(driver)) {
            return "jdbc:oracle:thin:@%s:%s:%s".formatted(url, port, service);
        } else if ("com.mysql.jdbc.Driver".equals(driver)) {
            return "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC".formatted(url, port, databaseName);
        } else if ("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(driver)) {
            return "jdbc:sqlserver://%s//%s;databaseName=%s".formatted(url, instance, databaseName);
        } else {
            return "jdbc:%s:%s:%s".formatted(url, port, service);
        }
    }
}
