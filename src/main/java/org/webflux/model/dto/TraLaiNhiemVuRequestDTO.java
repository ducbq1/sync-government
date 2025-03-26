package org.webflux.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TraLaiNhiemVuRequestDTO {
    @JsonProperty("session_id")
    private String sessionId;
    @JsonProperty("data")
    private Data data;

    @lombok.Data
    @Builder
    public static class Data {
        @JsonIgnore
        private Long id;
        @JsonProperty("MaNhiemVu")
        private String maNhiemVu;
        @JsonProperty("LyDoTraLai")
        private String lyDoTraLai;
        @JsonProperty("MaDonViDuocGiao")
        private String maDonViDuocGiao;
        @JsonProperty("TenDonViDuocGiao")
        private String tenDonViDuocGiao;
        @JsonProperty("CanBoTraLai")
        private String canBoTraLai;
        @JsonProperty("ThoiGianTraLai")
        private String thoiGianTraLai;
    }
}


