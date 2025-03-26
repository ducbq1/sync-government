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
public class TienDoXuLyNhiemVuRequestDTO {
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
        @JsonProperty("TrangThaiXuLy")
        private Long trangThaiXuLy;
        @JsonProperty("NgayHoanThanh")
        private String ngayHoanThanh;
        @JsonProperty("DienGiaiTrangThai")
        private String dienGiaiTrangThai;
        @JsonProperty("MaVanBanBaoCao")
        private String maVanBanBaoCao;
        @JsonProperty("MaDonViDuocGiao")
        private String maDonViDuocGiao;
        @JsonProperty("TenDonViDuocGiao")
        private String tenDonViDuocGiao;
        @JsonProperty("CanBoXuLy")
        private String canBoXuLy;
        @JsonProperty("ThoiGianCapNhat")
        private String thoiGianCapNhat;
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


