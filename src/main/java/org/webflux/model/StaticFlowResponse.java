package org.webflux.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StaticFlowResponse {
    private int total;
    private List<String> lstFlow;
    private List<String> lstSourceConfig;
    private List<String> lstDestinationConfig;
}