package org.webflux.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class BreadcrumbItem {
    private String label;
    private String url;
    private Boolean isActive;
}
