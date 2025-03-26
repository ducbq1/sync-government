package org.webflux.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DieuChinhThoiGianNhiemVuDTO {
    @JsonProperty("session_id")
    private String sessionId;
    @JsonProperty("data")
    private Data data;

    @lombok.Data
    @Builder
    public static class Data {
        @JsonIgnore
        private Long id;
        @JsonIgnore
        private String fileAttachs;
        @JsonProperty("MaNhiemVu")
        private String maNhiemVu;
        @JsonProperty("HanMoi")
        private String hanMoi;
        @JsonProperty("CanBoXuLy")
        private String canBoXuLy;
        @JsonProperty("VanBanDieuChinhThoiHan")
        private String vanBanDieuChinhThoiHan;
        @JsonProperty("DanhSachFileDinhKem")
        private List<DanhSachFileDinhKem> danhSachFileDinhKem;
    }

    @Builder
    public static class DanhSachFileDinhKem {
        @JsonProperty("TenFile")
        private String tenFile;
        @JsonProperty("NoiDungFile")
        private String noiDungFile;
    }
}


