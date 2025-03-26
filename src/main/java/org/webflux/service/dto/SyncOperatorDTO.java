package org.webflux.service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncOperatorDTO {
    private Long id;
    private String name;
    private String description;
    private String operatorData;
    private Long categoryId;
    private String idJson;
    private String body;
    private Long syncFlowId;
}
