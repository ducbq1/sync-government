package org.webflux.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MissionRequest {
    @JsonProperty("session_id")
    private String sessionId;
    @JsonProperty("data")
    private Data data;

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        @JsonProperty("MaDonViDuocGiao")
        private String maDonViDuocGiao;
        @JsonProperty("TuNgay")
        private String tuNgay;
        @JsonProperty("DenNgay")
        private String denNgay;
        @JsonProperty("trangthaicapnhat")
        private String trangThaiCapNhat;
    }
}
