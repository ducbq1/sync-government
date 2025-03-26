package org.webflux.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessData {
    @JsonProperty("status")
    private int status;
    @JsonProperty("error")
    private String error;
    @JsonProperty("message")
    private String message;
}
