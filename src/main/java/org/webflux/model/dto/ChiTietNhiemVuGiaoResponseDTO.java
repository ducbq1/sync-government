package org.webflux.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChiTietNhiemVuGiaoResponseDTO {
    @JsonProperty("status")
    private String status;
    @JsonProperty("message")
    private String message;
    @JsonProperty("item")
    private Item item;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @JsonProperty("MaNhiemVu")
        private String MaNhiemVu;
        @JsonProperty("NoiDungNhiemVu")
        private String NoiDungNhiemVu;
        @JsonProperty("ThuocCTCT")
        private String ThuocCTCT;
        @JsonProperty("MaVanBan")
        private String MaVanBan;
        @JsonProperty("MaDonViSoanThao")
        private String MaDonViSoanThao;
        @JsonProperty("DonViSoanThao")
        private String DonViSoanThao;
        @JsonProperty("ChuyenVienSoanThao")
        private String ChuyenVienSoanThao;
        @JsonProperty("ThoiGianTaoNhiemVu")
        private String ThoiGianTaoNhiemVu;
        @JsonProperty("MaDonViTheoDoi")
        private String MaDonViTheoDoi;
        @JsonProperty("DonViTheoDoi")
        private String DonViTheoDoi;
        @JsonProperty("LoaiThoiHanNhiemVu")
        private String LoaiThoiHanNhiemVu;
        @JsonProperty("HanXuLy")
        private String HanXuLy;
        @JsonProperty("SoLanDonDoc")
        private String SoLanDonDoc;
        @JsonProperty("MaTrangThai")
        private String MaTrangThai;
        @JsonProperty("TrangThaiCapNhat")
        private String TrangThaiCapNhat;
        @JsonProperty("Loai")
        private String Loai;
        @JsonProperty("ChuyenVienTheoDoi")
        private String ChuyenVienTheoDoi;
        @JsonProperty("ThongTinLienHe")
        private String ThongTinLienHe;
        @JsonProperty("ThoiGianDongBo")
        private String ThoiGianDongBo;
        @JsonProperty("MaDonViDuocGiao")
        private String MaDonViDuocGiao;
        @JsonProperty("TenDonViDuocGiao")
        private String TenDonViDuocGiao;
        @JsonProperty("TenCoQuanDuocGiao")
        private String TenCoQuanDuocGiao;
        @JsonProperty("PhamViCapNhat")
        private String PhamViCapNhat;
        @JsonProperty("DanhSachDonViPhoiHop")
        private List<DonViPhoiHop> DanhSachDonViPhoiHop;
        @JsonProperty("TienDoXuLy")
        private List<TienDoXuLy> TienDoXuLy;
        @JsonProperty("LichSuDieuChinhThoiHan")
        private List<LichSuDieuChinhThoiHan> LichSuDieuChinhThoiHan;
        @JsonProperty("LichSuGuiTraLaiBoDiaPhuong")
        private List<LichSuGuiTraLai> LichSuGuiTraLaiBoDiaPhuong;
        @JsonProperty("LichSuDonDocNhiemVu")
        private List<LichSuDonDoc> LichSuDonDocNhiemVu;
        @JsonProperty("VanBanPhatHanh")
        private VanBanPhatHanh VanBanPhatHanh;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DonViPhoiHop {
        @JsonProperty("MaCoQuan")
        private String MaCoQuan;
        @JsonProperty("CoQuan")
        private String CoQuan;
        @JsonProperty("DaDongBo")
        private String DaDongBo;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TienDoXuLy {
        @JsonProperty("MaLichSuTienDoXuLy")
        private String MaLichSuTienDoXuLy;
        @JsonProperty("NoiDung")
        private String NoiDung;
        @JsonProperty("MaTrangThai")
        private String MaTrangThai;
        @JsonProperty("NguoiCapNhat")
        private String NguoiCapNhat;
        @JsonProperty("NgayCapNhat")
        private String NgayCapNhat;
        @JsonProperty("FileDinhKem")
        private List<FileDinhKem> FileDinhKem;
        @JsonProperty("TrangThaiCapNhat")
        private String TrangThaiCapNhat;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LichSuGuiTraLai {
        @JsonProperty("MaLichSuGuiTra")
        private String MaLichSuGuiTra;
        @JsonProperty("NguoiGui")
        private String NguoiGui;
        @JsonProperty("NguoiNhan")
        private String NguoiNhan;
        @JsonProperty("NgayThucHien")
        private String NgayThucHien;
        @JsonProperty("TenDonViDuocGiao")
        private String TenDonViDuocGiao;
        @JsonProperty("MaDonViGiao")
        private String MaDonViGiao;
        @JsonProperty("NoiDung")
        private String NoiDung;
        @JsonProperty("TrangThaiThuHoiTraLai")
        private String TrangThaiThuHoiTraLai;
        @JsonProperty("TrangThaiCapNhat")
        private String TrangThaiCapNhat;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LichSuDieuChinhThoiHan {
        @JsonProperty("MaLichSuDieuChinhThoiHan")
        private String MaLichSuDieuChinhThoiHan;
        @JsonProperty("TrangThaiCapNhat")
        private String TrangThaiCapNhat;
        @JsonProperty("HanCu")
        private String HanCu;
        @JsonProperty("HanMoi")
        private String HanMoi;
        @JsonProperty("VanBanDieuChinhThoiHan")
        private String VanBanDieuChinhThoiHan;
        @JsonProperty("NguoiThucHien")
        private String NguoiThucHien;
        @JsonProperty("NgayThucHien")
        private String NgayThucHien;
        @JsonProperty("FileDinhKem")
        private List<FileDinhKem> FileDinhKem;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LichSuDonDoc {
        @JsonProperty("MaLichSuDonDoc")
        private String MaLichSuDonDoc;
        @JsonProperty("NgayDonDoc")
        private String NgayDonDoc;
        @JsonProperty("NoiDung")
        private String NoiDung;
        @JsonProperty("NguoiDonDoc")
        private String NguoiDonDoc;
        @JsonProperty("NgayTao")
        private String NgayTao;
        @JsonProperty("FileDinhKem")
        private List<FileDinhKem> FileDinhKem;
        @JsonProperty("HinhThucDonDoc")
        private String HinhThucDonDoc;
        @JsonProperty("TrangThaiCapNhat")
        private String TrangThaiCapNhat;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VanBanPhatHanh {
        @JsonProperty("TrichYeu")
        private String TrichYeu;
        @JsonProperty("DonViPhatHanh")
        private String DonViPhatHanh;
        @JsonProperty("ChuyenVien")
        private String ChuyenVien;
        @JsonProperty("NgayNhap")
        private String NgayNhap;
        @JsonProperty("SoKyHieu")
        private String SoKyHieu;
        @JsonProperty("NgayVanBan")
        private String NgayVanBan;
        @JsonProperty("LoaiYKien")
        private String LoaiYKien;
        @JsonProperty("DoMat")
        private String DoMat;
        @JsonProperty("NguoiKy")
        private String NguoiKy;
        @JsonProperty("ChucVu")
        private String ChucVu;
        @JsonProperty("FileDinhKem")
        private List<FileDinhKem> FileDinhKem;
    }
}
