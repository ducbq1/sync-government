package org.webflux.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NhiemVuGiaoRequestDTO {
    @JsonProperty("session_id")
    private String sessionId;
    @JsonProperty("data")
    private Data data;

    @Builder
    @lombok.Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {
        @JsonProperty("MaDonViDuocGiao")
        private String maDonViDuocGiao;
        @JsonProperty("TuNgay")
        private String tuNgay;
        @JsonProperty("DenNgay")
        private String denNgay;
        @JsonProperty("Trangthaicapnhat")
        private String trangThaiCapNhat;
    }
}


