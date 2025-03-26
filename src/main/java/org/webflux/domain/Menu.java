package org.webflux.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
public class Menu {

    public Menu(Long id, String title, String icon, String url, Long parentId, Long ordinal) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.url = url;
        this.parentId = parentId;
        this.ordinal = ordinal;
    }

    private Long id;
    private String title;
    private String icon;
    private String url;
    private Long parentId;
    private Long ordinal;
    private Set<Menu> children = new HashSet<>();
}
