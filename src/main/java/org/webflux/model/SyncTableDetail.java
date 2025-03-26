package org.webflux.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncTableDetail {
    private String id;
    private String functionBody;
    private String functionHead;
    private String name;
    private String value;
    private String syncTableId;
    private Long operatorId;
}