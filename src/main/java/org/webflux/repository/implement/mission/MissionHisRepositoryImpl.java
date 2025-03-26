package org.webflux.repository.implement.mission;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.ibm.icu.impl.Pair;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.webflux.domain.mission.MissionHisEntity;
import org.webflux.domain.mapper.mission.MissionHisEntityRowMapper;
import org.webflux.helper.StringUtils;
import org.webflux.model.dto.DieuChinhThoiGianNhiemVuDTO;
import org.webflux.model.dto.GuiBaoCaoNhiemVuRequestDTO;
import org.webflux.model.dto.TienDoXuLyNhiemVuRequestDTO;
import org.webflux.model.dto.TraLaiNhiemVuRequestDTO;
import org.webflux.repository.LogRepository;
import org.webflux.repository.mission.MissionHisRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.webflux.enumerate.MissionAction.*;

@Repository
public class MissionHisRepositoryImpl implements MissionHisRepository {
    private static final Logger log = LoggerFactory.getLogger(MissionHisRepositoryImpl.class);
    private final LogRepository logRepository;

    public MissionHisRepositoryImpl(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public MissionHisEntity saveOrUpdate(JdbcTemplate jdbcTemplate, MissionHisEntity entity) {
        entity.setSendDate(new Date(System.currentTimeMillis()));
        MissionHisEntity existingEntity = findById(jdbcTemplate, entity.getMissionHisId());
        if (Objects.nonNull(existingEntity) && Objects.nonNull(existingEntity.getMissionHisId())) {
            update(jdbcTemplate, entity);
        } else {
            insert(jdbcTemplate, entity);
        }
        return entity;
    }

    @Override
    public MissionHisEntity findById(JdbcTemplate jdbcTemplate, Long id) {
        String sql = "SELECT * FROM mission_his WHERE mission_his_id = ?";
        try {
            List<MissionHisEntity> lstResult = jdbcTemplate.query(sql, new Object[]{id}, new MissionHisEntityRowMapper());
            if (!CollectionUtils.isEmpty(lstResult)) {
                return lstResult.get(0);
            }
        } catch (Exception ex) {
        }
        return null;
    }

    public int insert(JdbcTemplate jdbcTemplate, MissionHisEntity entity) {
        String sql = "INSERT INTO mission_his (mission_his_id, mission_id, user_id, action_type, content, create_time, receiver_id, receiver_type, default_user_rec, detail_id, report_info, file_attachs, code, type, report_content, status, deadline_old, deadline_new, update_by_id, update_by, update_time, to_identify_code, to_dept_name, document_id, document_code, abstract_document, is_edoc, is_sent, message_log, send_date) " +
                "VALUES (mission_his_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int result = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"mission_his_id"});
            ps.setObject(1, entity.getMissionId());
            ps.setObject(2, entity.getUserId());
            ps.setObject(3, entity.getActionType());
            ps.setString(4, entity.getContent());
            ps.setDate(5, entity.getCreateTime());
            ps.setObject(6, entity.getReceiverId());
            ps.setObject(7, entity.getReceiverType());
            ps.setObject(8, entity.getDefaultUserRec());
            ps.setObject(9, entity.getDetailId());
            ps.setString(10, entity.getReportInfo());
            ps.setString(11, entity.getFileAttachs());
            ps.setString(12, entity.getCode());
            ps.setObject(13, entity.getType());
            ps.setString(14, entity.getReportContent());
            ps.setObject(15, entity.getStatus());
            ps.setDate(16, entity.getDeadlineOld());
            ps.setDate(17, entity.getDeadlineNew());
            ps.setObject(18, entity.getUpdateById());
            ps.setString(19, entity.getUpdateBy());
            ps.setDate(20, entity.getUpdateTime());
            ps.setString(21, entity.getToIdentifyCode());
            ps.setString(22, entity.getToDeptName());
            ps.setObject(23, entity.getDocumentId());
            ps.setString(24, entity.getDocumentCode());
            ps.setString(25, entity.getAbstractDocument());
            ps.setString(26, entity.getIsEdoc());
            ps.setObject(27, entity.getIsSent());
            ps.setString(28, entity.getMessageLog());
            ps.setDate(29, entity.getSendDate());
            return ps;
        }, keyHolder);
        entity.setMissionHisId(keyHolder.getKey().longValue());
        return result;
    }

    public int update(JdbcTemplate jdbcTemplate, MissionHisEntity entity) {
        String sql = "UPDATE mission_his SET mission_id = ?, user_id = ?, action_type = ?, content = ?, create_time = ?, receiver_id = ?, receiver_type = ?, default_user_rec = ?, detail_id = ?, report_info = ?, file_attachs = ?, code = ?, type = ?, report_content = ?, status = ?, deadline_old = ?, deadline_new = ?, update_by_id = ?, update_by = ?, update_time = ?, to_identify_code = ?, to_dept_name = ?, document_id = ?, document_code = ?, abstract_document = ?, is_edoc = ?, is_sent = ?, message_log = ?, send_date = ? " +
                "WHERE mission_his_id = ?";
        return jdbcTemplate.update(sql, entity.getMissionId(), entity.getUserId(), entity.getActionType(), entity.getContent(), entity.getCreateTime(), entity.getReceiverId(), entity.getReceiverType(), entity.getDefaultUserRec(), entity.getDetailId(), entity.getReportInfo(), entity.getFileAttachs(), entity.getCode(), entity.getType(), entity.getReportContent(), entity.getStatus(), entity.getDeadlineOld(), entity.getDeadlineNew(), entity.getUpdateById(), entity.getUpdateBy(), entity.getUpdateTime(), entity.getToIdentifyCode(), entity.getToDeptName(), entity.getDocumentId(), entity.getDocumentCode(), entity.getAbstractDocument(), entity.getIsEdoc(), entity.getIsSent(), entity.getMessageLog(), entity.getSendDate(), entity.getMissionHisId());
    }

    public List<String> getStringTraLaiNhiemVu(JdbcTemplate jdbcTemplate) {
        String sql = """
                select mission_his_id, code, report_content, report_info, to_identify_code, to_dept_name, update_by, update_time from mission_his 
                --where status = 10 and is_edoc = 1 and type = 2
                """;
        try {
            var jsonMain = JsonNodeFactory.instance.objectNode();
            jsonMain.set("session_id", JsonNodeFactory.instance.textNode("rmCGoSh8khtbSJgOgHBFFqo4UVk"));
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                var json = JsonNodeFactory.instance.objectNode();
                json.set("MaNhiemVu", JsonNodeFactory.instance.textNode(rs.getString("code")));
                json.set("LyDoTraLai", JsonNodeFactory.instance.textNode(rs.getString("report_info")));
                json.set("MaDonViDuocGiao", JsonNodeFactory.instance.textNode(rs.getString("to_identify_code")));
                json.set("TenDonViDuocGiao", JsonNodeFactory.instance.textNode(rs.getString("to_dept_name")));
                json.set("CanBoTraLai", JsonNodeFactory.instance.textNode(rs.getString("update_by")));
                json.set("ThoiGianTraLai", JsonNodeFactory.instance.textNode(rs.getString("update_time")));
                jsonMain.set("data", json);
                return jsonMain.toString();
            });
        } catch (DataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public List<TraLaiNhiemVuRequestDTO> getBodyTraLaiNhiemVu(JdbcTemplate jdbcTemplate) {
        String sql = """
                select mission_his_id, code, report_content, report_info, to_identify_code, to_dept_name, update_by, update_time from mission_his 
                where is_edoc = 1 and type = 2 and nvl(is_sent, 0) = 0
                """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                TraLaiNhiemVuRequestDTO obj = new TraLaiNhiemVuRequestDTO();
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");

                obj.setData(obj.getData().builder()
                        .id(rs.getLong("mission_his_id"))
                        .maNhiemVu(rs.getString("code"))
                        .lyDoTraLai(rs.getString("report_info"))
                        .maDonViDuocGiao(rs.getString("to_identify_code"))
                        .tenDonViDuocGiao(rs.getString("to_dept_name"))
                        .canBoTraLai(rs.getString("update_by"))
                        .thoiGianTraLai(dateFormat.format(Objects.nonNull(rs.getDate("update_time")) ? rs.getDate("update_time") : new Date(System.currentTimeMillis())))
                        .build());
                return obj;
            });
        } catch (DataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public List<GuiBaoCaoNhiemVuRequestDTO> getBodyGuiBaoCao(JdbcTemplate jdbcTemplate) {
        String sql = """
                select mission_his_id, code, report_content, to_identify_code, to_dept_name, update_by, update_time from mission_his
                where status = 3 and is_edoc = 1 and type = 3 and nvl(is_sent, 0) = 0
                """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                GuiBaoCaoNhiemVuRequestDTO obj = new GuiBaoCaoNhiemVuRequestDTO();
                obj.setData(obj.getData().builder()
                        .id(rs.getLong("mission_his_id"))
                        .maNhiemVu(rs.getString("code"))
                        .canBoXuLy(rs.getString("update_by"))
                        .build());
                return obj;
            });
        } catch (DataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public List<TienDoXuLyNhiemVuRequestDTO> getBodyTienDoXuLy(JdbcTemplate jdbcTemplate, String basePath) {
        String sql = """
                select mission_his_id, code, status, create_time,
                report_content, report_info, to_identify_code, to_dept_name, update_by, update_time, file_attachs from mission_his
                where is_edoc = 1 and type = 1 and nvl(is_sent, 0) = 0
                """;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            DateFormat dateFormatShort = new SimpleDateFormat("yyyyMMdd");
            var listHis = jdbcTemplate.query(sql, (rs, rowNum) -> {
                TienDoXuLyNhiemVuRequestDTO obj = new TienDoXuLyNhiemVuRequestDTO();
                obj.setData(obj.getData().builder()
                        .id(rs.getLong("mission_his_id"))
                        .maNhiemVu(rs.getString("code"))
                        .trangThaiXuLy(rs.getLong("status"))
                        .ngayHoanThanh(dateFormatShort.format(Objects.nonNull(rs.getDate("create_time")) ? rs.getDate("create_time") : new Date(System.currentTimeMillis())))
                        .dienGiaiTrangThai(rs.getString("report_info"))
                        .maVanBanBaoCao("")
                        .maDonViDuocGiao(rs.getString("to_identify_code"))
                        .tenDonViDuocGiao(rs.getString("to_dept_name"))
                        .canBoXuLy(rs.getString("update_by"))
                        .thoiGianCapNhat(dateFormat.format(Objects.nonNull(rs.getDate("update_time")) ? rs.getDate("update_time") : new Date(System.currentTimeMillis())))
                        .fileAttachs(rs.getString("file_attachs"))
                        .build());
                return obj;
            });

            List<TienDoXuLyNhiemVuRequestDTO> newList = new ArrayList<>();
            for (var his : listHis) {
                if (Objects.nonNull(his.getData().getFileAttachs()) && !his.getData().getFileAttachs().isBlank()) {
                    List<TienDoXuLyNhiemVuRequestDTO.DanhSachFileDinhKem> listFile = Arrays.stream(his.getData().getFileAttachs().split(",")).map(
                            attach -> {
                                try {
                                    var file = jdbcTemplate.queryForObject("select * from attachs where rownum = 1 and attach_id = ?", new Object[]{attach},
                                            new int[]{Types.VARCHAR},
                                            (rs, rowNum) -> Pair.of(rs.getString("attach_name"), rs.getString("attach_path")));
                                    Path filePath = Path.of(basePath, file.second);
                                    String content = "";
                                    content = Base64.getEncoder().encodeToString(Files.readAllBytes(filePath));

                                    return TienDoXuLyNhiemVuRequestDTO.DanhSachFileDinhKem
                                            .builder()
                                            .tenFile(file.first)
                                            .noiDungFile(content)
                                            .build();

                                } catch (IOException ex) {
                                    log.error(ex.getMessage(), ex);
                                } catch (DataAccessException ex) {
                                    log.error(ex.getMessage(), ex);
                                }
                                return null;
                            }).collect(Collectors.toList());
                    var data = his.getData();
                    data.setDanhSachFileDinhKem(listFile);
                    his.setData(data);
                    newList.add(his);
                } else {
                    var missionHis = findById(jdbcTemplate, his.getData().getId());
                    missionHis.setIsSent(1L);
                    missionHis.setMessageLog(KHONG_CO_TEP_TIN);
                    update(jdbcTemplate, missionHis);
                    if (jdbcTemplate.getDataSource() instanceof HikariDataSource dataSource) {
                        logRepository.log(CAP_NHAT_TIEN_DO, StringUtils.merge(his.getData().getMaNhiemVu(), KHONG_CO_TEP_TIN), dataSource.getJdbcUrl(), 0L, Boolean.TRUE);
                    } else {
                        logRepository.log(CAP_NHAT_TIEN_DO, StringUtils.merge(his.getData().getMaNhiemVu(), KHONG_CO_TEP_TIN), jdbcTemplate.getDataSource().toString(), 0L, Boolean.TRUE);
                    }
                }
            }
            return newList;
        } catch (DataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public List<DieuChinhThoiGianNhiemVuDTO> getBodyDieuChinhThoiGianNhiemVu(JdbcTemplate jdbcTemplate, String basePath) {
        String sql = """
                select mission_his_id, code, deadline_new, update_by, file_attachs from mission_his
                where is_edoc = 1 and type = 4 and code is not null and nvl(is_sent, 0) = 0
                """;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            var listHis = jdbcTemplate.query(sql, (rs, rowNum) -> {
                DieuChinhThoiGianNhiemVuDTO obj = new DieuChinhThoiGianNhiemVuDTO();
                obj.setData(obj.getData().builder()
                        .id(rs.getLong("mission_his_id"))
                        .maNhiemVu(rs.getString("code"))
                        .hanMoi(dateFormat.format(Objects.nonNull(rs.getDate("deadline_new")) ? rs.getDate("deadline_new") : new Date(System.currentTimeMillis())))
                        .canBoXuLy(rs.getString("update_by"))
                        .fileAttachs(rs.getString("file_attachs"))
                        .build());
                return obj;
            });

            List<DieuChinhThoiGianNhiemVuDTO> newList = new ArrayList<>();
            for (var his : listHis) {
                if (Objects.nonNull(his.getData().getFileAttachs()) && !his.getData().getFileAttachs().isBlank()) {
                    List<DieuChinhThoiGianNhiemVuDTO.DanhSachFileDinhKem> listFile = Arrays.stream(his.getData().getFileAttachs().split(",")).map(
                            attach -> {
                                try {
                                    var file = jdbcTemplate.queryForObject("select * from attachs where rownum = 1 and attach_id = ?", new Object[]{attach},
                                            new int[]{Types.INTEGER},
                                            (rs, rowNum) -> Pair.of(rs.getString("attach_name"), rs.getString("attach_path")));
                                    Path filePath = Path.of(basePath, file.second);
                                    String content = "";
                                    content = Base64.getEncoder().encodeToString(Files.readAllBytes(filePath));

                                    return DieuChinhThoiGianNhiemVuDTO.DanhSachFileDinhKem
                                            .builder()
                                            .tenFile(file.first)
                                            .noiDungFile(content)
                                            .build();

                                } catch (IOException ex) {
                                    log.error(ex.getMessage(), ex);
                                } catch (DataAccessException ex) {
                                    log.error(ex.getMessage(), ex);
                                }
                                return null;
                            }).collect(Collectors.toList());
                    var data = his.getData();
                    data.setDanhSachFileDinhKem(listFile);
                    his.setData(data);
                    newList.add(his);
                } else {
                    var missionHis = findById(jdbcTemplate, his.getData().getId());
                    missionHis.setIsSent(1L);
                    missionHis.setMessageLog(KHONG_CO_TEP_TIN);
                    update(jdbcTemplate, missionHis);
                    if (jdbcTemplate.getDataSource() instanceof HikariDataSource dataSource) {
                        logRepository.log(DIEU_CHINH_THOI_GIAN, StringUtils.merge(his.getData().getMaNhiemVu(), KHONG_CO_TEP_TIN), dataSource.getJdbcUrl(), 0L, Boolean.TRUE);
                    } else {
                        logRepository.log(DIEU_CHINH_THOI_GIAN, StringUtils.merge(his.getData().getMaNhiemVu(), KHONG_CO_TEP_TIN), jdbcTemplate.getDataSource().toString(), 0L, Boolean.TRUE);
                    }
                }
            }
            return newList;
        } catch (DataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
}
