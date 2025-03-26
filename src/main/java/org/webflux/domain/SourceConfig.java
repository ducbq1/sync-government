package org.webflux.domain;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SourceConfig extends EntityBase {
    private String name;
    private String description;
}
