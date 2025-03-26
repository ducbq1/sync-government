package org.webflux.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaiFileDinhKemRequestDTO {
    @JsonProperty("session_id")
    private String sessionId;
    @JsonProperty("data")
    private Data data;

    @Builder
    public static class Data {
        @JsonProperty("IDFileDinhKem")
        private String IDFileDinhKem;
        @JsonProperty("TypeId")
        private String TypeId;
    }
}


