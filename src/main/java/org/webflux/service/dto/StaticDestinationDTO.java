package org.webflux.service.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"id", "name", "description", "url", "isActivated", "isConnected", "editButton"})
public class StaticDestinationDTO {
    private Long id;
    private String name;
    private String description;
    private String url;
    private Boolean isActivated;
    private Boolean isConnected;
    private StaticFlowDTO.Button editButton;
    private StaticFlowDTO.Button checkButton;

    @Data
    @Builder
    public static class Button {
        private String icon;
        private String callback;
    }
}