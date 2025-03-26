package org.webflux.repository.query;

import lombok.Builder;
import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Builder
public class SyncFlowStaticQuery {

    private static final Pattern HOST_PORT_PATTERN = Pattern.compile("^(https?|ftp|file|static|dns)://([\\w.-]+):(\\d+)$");

    private Long id;
    private String content;
    private String token;
    private String url;
    private Integer port;
    private String userName;
    private String password;
    private String driver;
    private String service;
    private String databaseName;
    private String instance;
    private String proxy;
    private String payload;
    private String saveFilePath;
    private Boolean isGetSyncedAgain;

    public String getHostProxy() {
        Matcher matcher = HOST_PORT_PATTERN.matcher(proxy);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return null;
    }
    public Integer getPortProxy() {
        Matcher matcher = HOST_PORT_PATTERN.matcher(proxy);
        if (matcher.matches()) {
            try {
                return Integer.parseInt(matcher.group(3));
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    public String getUrl() {
        if ("oracle.jdbc.OracleDriver".equals(driver)) {
            return "jdbc:oracle:thin:@%s:%s/%s".formatted(url, port, service);
        } else if ("com.mysql.jdbc.Driver".equals(driver)) {
            return "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC".formatted(url, port, databaseName);
        } else if ("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(driver)) {
            return "jdbc:sqlserver://%s//%s;databaseName=%s".formatted(url, instance, databaseName);
        } else {
            return "jdbc:%s:%s:%s".formatted(url, port, service);
        }
    }
}
