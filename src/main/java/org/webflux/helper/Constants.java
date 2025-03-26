package org.webflux.helper;

public class Constants {
    public static String FILE_MESS = "message";
    public static final String MA_TINH_NAM_DINH = "H40";

    public interface MessCode {
        int SUCCESS = 1;
        int FAIL = 0;
    }

    public interface Pattern {
        String REGEX_SPLIT_IMPORT = ",";
    }

    public interface EDOC_RECEIVE_MISSION_HIS_TYPE  {
        Long TIEN_DO_XU_LY = 1L;
        Long LICH_SU_GUI_TRA = 2L;
        Long LICH_SU_DON_DOC = 3L;
        Long LICH_SU_DIEU_CHINH_THOI_HAN = 4L;
    }

    public interface LoaiNhiemVu {
        String GIAO_CHU_TRI = "1";
        String PHOI_HOP = "2";
    }
}
