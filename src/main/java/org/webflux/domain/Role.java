package org.webflux.domain;

import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class Role {
    private Long id;
    private String name;
    private String displayName;
    private Long userId;
    private Long privilegeId;
    @Builder.Default
    private Set<User> users = new HashSet<>();
    @Builder.Default
    private Set<Privilege> privileges = new HashSet<>();
    @Builder.Default
    private Set<Menu> menus = new HashSet<>();
}
