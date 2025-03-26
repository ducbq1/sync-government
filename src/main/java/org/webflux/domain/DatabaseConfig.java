package org.webflux.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseConfig {
    private Long id;
    private String name;
    private String description;
    private String url;
    private String userName;
    private String password;
    private String driver;
    private Integer port;
    private Boolean isConnected;
    private Boolean isActivated;
    private Boolean isDeleted;
    private Long createdBy;
    private String createdAt;
    private Long updatedBy;
    private String updatedAt;
    private String service;
}