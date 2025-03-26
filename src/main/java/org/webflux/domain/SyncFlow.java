package org.webflux.domain;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncFlow {
    private Long id;
    private String name;
    private String description;
    private String flowData;
}
