package org.webflux.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncTable {
    private String id;
    private String name;
    private String action;
    private String sequence;
    private String uniqueKey;
    private String tableComments;
    private String primaryKey;
    private Long operatorId;
    private String parentId;
}