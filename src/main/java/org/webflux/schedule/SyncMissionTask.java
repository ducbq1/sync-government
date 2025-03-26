package org.webflux.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.webflux.config.ClientFactory;
import org.webflux.domain.mission.EdocReceiveFileAttachEntity;

import org.webflux.helper.CommonUtils;
import org.webflux.helper.Constants;
import org.webflux.helper.StringUtils;
import org.webflux.model.MissionRequest;
import org.webflux.model.dto.*;
import org.webflux.repository.query.SyncFlowStaticQuery;
import org.webflux.service.AuditLogService;
import org.webflux.service.StaticFlowService;
import org.webflux.service.mission.*;
import reactor.core.publisher.Flux;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.webflux.enumerate.MissionAction.*;
import static org.webflux.helper.Constants.MA_TINH_NAM_DINH;

@Log4j2
@Component
public class SyncMissionTask {
    private EdocReceiveMissionService edocReceiveMissionService;
    private MissionService missionService;
    private EdocReceiveFileAttachService edocReceiveFileAttachService;
    private AttachsService attachsService;
    private MissionDetailService missionDetailService;
    private MissionHisService missionHisService;
    private AuditLogService logService;
    private StaticFlowService staticFlowService;

    public SyncMissionTask(EdocReceiveMissionService edocReceiveMissionService,
                           MissionService missionService,
                           EdocReceiveFileAttachService edocReceiveFileAttachService,
                           AttachsService attachsService,
                           MissionDetailService missionDetailService,
                           MissionHisService missionHisService,
                           StaticFlowService staticFlowService,
                           AuditLogService logService) throws ClassNotFoundException {
        this.edocReceiveFileAttachService = edocReceiveFileAttachService;
        this.missionService = missionService;
        this.edocReceiveMissionService = edocReceiveMissionService;
        this.attachsService = attachsService;
        this.missionDetailService = missionDetailService;
        this.missionHisService = missionHisService;
        this.staticFlowService = staticFlowService;
        this.logService = logService;
    }

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    @Value("${MISSION_LONG}")
    String MISSION_LONG;

    @Value("${MISSION_DATE}")
    String MISSION_DATE;

    @Value("${MISSION_OBJECT}")
    String MISSION_OBJECT;

    @Value("${MISSION_LIST}")
    String MISSION_LIST;

    public void runSyncNhiemVu() {
        var syncFlowStatics = staticFlowService.findAll();
        if (CollectionUtils.isEmpty(syncFlowStatics)) {
            return;
        }

        Flux.fromIterable(syncFlowStatics).subscribe(
                base -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        MissionRequest missionRequest = new MissionRequest();
                        if (Objects.nonNull(base.getPayload()) && !base.getPayload().isBlank()) {
                            missionRequest = objectMapper.readValue(base.getPayload(), MissionRequest.class);
                        }

                        String maDonViDuocGiao = Objects.nonNull(missionRequest.getData().getMaDonViDuocGiao()) ? missionRequest.getData().getMaDonViDuocGiao() : MA_TINH_NAM_DINH;
                        String dateFrom;
                        String dateTo;
                        if (Objects.nonNull(missionRequest.getData())
                                && Objects.nonNull(missionRequest.getData().getTuNgay())
                                && Objects.nonNull(missionRequest.getData().getDenNgay())
                                && Objects.nonNull(CommonUtils.convertStringToDateWithFormat(missionRequest.getData().getTuNgay(), "yyyyMMdd"))
                                && Objects.nonNull(CommonUtils.convertStringToDateWithFormat(missionRequest.getData().getDenNgay(), "yyyyMMdd"))) {
                            dateFrom = missionRequest.getData().getTuNgay();
                            dateTo = missionRequest.getData().getDenNgay();
                        } else {
                            java.util.Date referenceDate = new Date();

                            Calendar to = Calendar.getInstance();
                            to.setTime(referenceDate);

                            Calendar from = Calendar.getInstance();
                            from.setTime(referenceDate);
                            from.add(Calendar.MONTH, -6);
                            from.add(Calendar.DATE, 5);
                            dateFrom = new SimpleDateFormat("yyyyMMdd").format(from.getTime());
                            dateTo = new SimpleDateFormat("yyyyMMdd").format(to.getTime());
                        }
                        DataSource dataSources = ClientFactory.getDataSource(base.getDriver(), base.getUrl(), base.getUserName(), base.getPassword());
                        if (dataSources instanceof HikariDataSource dataSource) {
                            try (dataSource) {
                                AtomicInteger countNhiemVu = new AtomicInteger(1);
                                String url = getUrlAPI(base.getContent(), "NhiemVuGiao", true);
                                NhiemVuGiaoRequestDTO requestDTO = new NhiemVuGiaoRequestDTO();
                                requestDTO.setSessionId(missionRequest.getSessionId());
                                requestDTO.setData(NhiemVuGiaoRequestDTO.Data.builder()
                                        .maDonViDuocGiao(maDonViDuocGiao)
                                        .tuNgay(dateFrom)
                                        .denNgay(dateTo)
                                        .trangThaiCapNhat(Objects.nonNull(missionRequest.getData().getTrangThaiCapNhat()) ? missionRequest.getData().getTrangThaiCapNhat() : "0")
                                        .build());
                                HttpClient client = checkProxy(base.getHostProxy(), base.getPortProxy())
                                        ? HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(base.getHostProxy(), base.getPortProxy()))).build()
                                        : HttpClient.newHttpClient();
                                HttpRequest request = HttpRequest.newBuilder()
                                        .uri(new URI(url))
                                        .header("Content-Type", "application/json")
                                        .header("Authorization", "Bearer " + base.getToken())
                                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestDTO), StandardCharsets.UTF_8))
                                        .build();
                                HttpResponse<String> responseEntity = client.send(request, HttpResponse.BodyHandlers.ofString());

                                NhiemVuGiaoResponseDTO nvGiao;
                                if (responseEntity.statusCode() == HttpStatus.OK.value()) {
                                    ResponseDTO response = objectMapper.readValue(responseEntity.body(), ResponseDTO.class);
                                    if (Objects.nonNull(response) && response.getMess().getMessCode() == Constants.MessCode.SUCCESS) {
                                        nvGiao = objectMapper.readValue(response.getData(), NhiemVuGiaoResponseDTO.class);
                                        if (Objects.nonNull(nvGiao) && !CollectionUtils.isEmpty(nvGiao.getNhiemVu())) {
                                            for (NhiemVuGiaoResponseDTO.NhiemVu nv : nvGiao.getNhiemVu()) {
                                                System.out.println(ANSI_RED + "Nhiệm vụ: " + countNhiemVu.getAndIncrement() + ANSI_RESET);
                                                log.info("Nhiệm vụ: " + countNhiemVu.get());
                                                if (Objects.nonNull(nv) && checkValidateFieldMission(nv).isEmpty()) {
                                                    edocReceiveMissionService.saveEdocReceiveMission(dataSource, nv);
                                                    ChiTietNhiemVuGiaoResponseDTO nvChiTiet = getChiTietNhiemVuGiaoFromApi(nv.getMaNhiemVu(), base, missionRequest.getSessionId());
                                                    if (Objects.nonNull(nvChiTiet) && HttpStatus.OK.value() == Integer.parseInt(nvChiTiet.getStatus())) {
                                                        ChiTietNhiemVuGiaoResponseDTO.Item itemCt = nvChiTiet.getItem();
                                                        if (Objects.nonNull(itemCt) && itemCt.getMaDonViDuocGiao().equals(maDonViDuocGiao) && checkValidateFieldMission(itemCt).isEmpty()) {
                                                            // save mission, mission detail, mission his
                                                            missionService.saveMission(dataSource, nv, itemCt, maDonViDuocGiao, base.getIsGetSyncedAgain(), base.getUrl());
                                                            //save edoc
                                                            edocReceiveMissionService.saveEdocReceiveMissionDetail(dataSource, itemCt, maDonViDuocGiao);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        }


                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                });

    }

    @Scheduled(fixedDelay = 10 * 1000)
    public void runSyncFileAttach() {
        var syncFlowStatics = staticFlowService.findAll();
        if (CollectionUtils.isEmpty(syncFlowStatics)) {
            return;
        }

        Flux.fromIterable(syncFlowStatics).subscribe(
                base -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        MissionRequest missionRequest = new MissionRequest();
                        if (Objects.nonNull(base.getPayload()) && !base.getPayload().isBlank()) {
                            missionRequest = objectMapper.readValue(base.getPayload(), MissionRequest.class);
                        }

                        String maDonViDuocGiao = Objects.nonNull(missionRequest.getData().getMaDonViDuocGiao()) ? missionRequest.getData().getMaDonViDuocGiao() : MA_TINH_NAM_DINH;
                        DataSource dataSources = ClientFactory.getDataSource(base.getDriver(), base.getUrl(), base.getUserName(), base.getPassword());
                        if (dataSources instanceof HikariDataSource dataSource) {
                            try (dataSource) {
                                List<EdocReceiveFileAttachEntity> lstFileNotSync = edocReceiveFileAttachService.findByIsSync(dataSource, 0L, 50L);
                                if (!CollectionUtils.isEmpty(lstFileNotSync)) {
                                    for (EdocReceiveFileAttachEntity fileAttach : lstFileNotSync) {
                                        String fileBase64 = getFileDinhKemFromApi(fileAttach, base, missionRequest.getSessionId());
                                        attachsService.saveFileDinhKem(dataSource, fileAttach, fileBase64, base);
                                    }
                                }
                            } catch (Exception ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        }


                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
        );

    }

    public void runSyncBackOptimizeVPCP() {
        var syncFlowStatics = staticFlowService.findAll();
        if (CollectionUtils.isEmpty(syncFlowStatics)) {
            return;
        }

        Flux.fromIterable(syncFlowStatics)
                .subscribe(base -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        DataSource dataSources = ClientFactory.getDataSource(base.getDriver(), base.getUrl(), base.getUserName(), base.getPassword());
                        if (dataSources instanceof HikariDataSource dataSource) {
                            try (dataSource) {
                                var traLaiNhiemVus = missionHisService.getStringTraLaiNhiemVu(dataSource);
                                String url = "https://api.namdinh.gov.vn/apitdnv/TraLaiNhiemVu?isUrltest=1";
                                for (var traLaiNhiemVu : traLaiNhiemVus) {
                                    HttpClient client = checkProxy(base.getHostProxy(), base.getPortProxy())
                                            ? HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(base.getHostProxy(), base.getPortProxy()))).build()
                                            : HttpClient.newHttpClient();
                                    HttpRequest request = HttpRequest.newBuilder()
                                            .uri(new URI(url))
                                            .header("Content-Type", "application/json")
                                            .header("Authorization", "Bearer " + base.getToken())
                                            .POST(HttpRequest.BodyPublishers.ofString(traLaiNhiemVu, StandardCharsets.UTF_8))
                                            .build();
                                    HttpResponse<String> responseEntity = client.send(request, HttpResponse.BodyHandlers.ofString());
                                    if (responseEntity.statusCode() == HttpStatus.OK.value()) {
                                        InteractResponseDTO response = objectMapper.readValue(responseEntity.body(), InteractResponseDTO.class);
                                        if (Objects.nonNull(response)) {
                                            MessData messData = objectMapper.readValue(response.getData(), MessData.class);
                                            if (response.getMess().getMessCode() == Constants.MessCode.SUCCESS && messData.getStatus() == HttpStatus.OK.value()) {
                                                System.out.println("DONE");
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        }


                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                });

    }

    @Scheduled(fixedDelay = 10 * 1000)
    public void runSyncBackVPCP() {

        var syncFlowStatics = staticFlowService.findAll();
        if (CollectionUtils.isEmpty(syncFlowStatics)) {
            return;
        }

        Flux.fromIterable(syncFlowStatics)
                .subscribe(base -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        DataSource dataSources = ClientFactory.getDataSource(base.getDriver(), base.getUrl(), base.getUserName(), base.getPassword());

                        if (dataSources instanceof HikariDataSource dataSource) {
                            try (dataSource) {
                                HttpClient client = checkProxy(base.getHostProxy(), base.getPortProxy())
                                        ? HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(base.getHostProxy(), base.getPortProxy()))).build()
                                        : HttpClient.newHttpClient();

                                String url = String.format("%s/TraLaiNhiemVu?isUrltest=1", base.getContent());
                                MissionRequest missionRequest = new MissionRequest();
                                if (Objects.nonNull(base.getPayload()) && !base.getPayload().isBlank()) {
                                    missionRequest = objectMapper.readValue(base.getPayload(), MissionRequest.class);
                                }

                                List<TraLaiNhiemVuRequestDTO> requestDTOS = missionHisService.getBodyTraLaiNhiemVu(dataSource);
                                if (CollectionUtils.isEmpty(requestDTOS)) return;
                                for (var requestDTO : requestDTOS) {
                                    requestDTO.setSessionId(missionRequest.getSessionId());
                                    HttpRequest request = HttpRequest.newBuilder()
                                            .uri(new URI(url))
                                            .header("Content-Type", "application/json")
                                            .header("Authorization", "Bearer " + base.getToken())
                                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestDTO), StandardCharsets.UTF_8))
                                            .build();
                                    HttpResponse<String> responseEntity = client.send(request, HttpResponse.BodyHandlers.ofString());
                                    if (responseEntity.statusCode() == HttpStatus.OK.value()) {
                                        InteractResponseDTO response = objectMapper.readValue(responseEntity.body(), InteractResponseDTO.class);
                                        if (Objects.nonNull(response)) {
                                            MessData messData = objectMapper.readValue(response.getData(), MessData.class);
                                            if (response.getMess().getMessCode() == Constants.MessCode.SUCCESS && messData.getStatus() == HttpStatus.OK.value()) {
                                                var missionHis = missionHisService.findById(dataSource, requestDTO.getData().getId());
                                                missionHis.setIsSent(1L);
                                                missionHisService.saveOrUpdate(dataSource, missionHis);
                                                logService.log(TRA_LAI, StringUtils.merge(missionHis.getCode(), messData.getMessage()), base.getUrl(), false);
                                                log.info(String.format("%s - %s - %s", requestDTO.getData().getMaNhiemVu(), messData.getError(), messData.getMessage()));
                                            } else {
                                                var missionHis = missionHisService.findById(dataSource, requestDTO.getData().getId());
                                                missionHis.setIsSent(1L);
                                                missionHis.setMessageLog(messData.getMessage());
                                                missionHisService.saveOrUpdate(dataSource, missionHis);
                                                logService.log(TRA_LAI, StringUtils.merge(missionHis.getCode(), messData.getMessage(), messData.getError()), base.getUrl(), true);
                                                log.error(String.format("%s - %s - %s", requestDTO.getData().getMaNhiemVu(), messData.getError(), messData.getMessage()));
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        }


                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                });

    }

    public void runSyncReportVPCP() {
        var syncFlowStatics = staticFlowService.findAll();
        if (CollectionUtils.isEmpty(syncFlowStatics)) {
            return;
        }

        Flux.fromIterable(syncFlowStatics)
                .subscribe(base -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        MissionRequest missionRequest = new MissionRequest();
                        if (Objects.nonNull(base.getPayload()) && !base.getPayload().isBlank()) {
                            missionRequest = objectMapper.readValue(base.getPayload(), MissionRequest.class);
                        }
                        DataSource dataSources = ClientFactory.getDataSource(base.getDriver(), base.getUrl(), base.getUserName(), base.getPassword());

                        if (dataSources instanceof HikariDataSource dataSource) {
                            try (dataSource) {
                                HttpClient client = checkProxy(base.getHostProxy(), base.getPortProxy())
                                        ? HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(base.getHostProxy(), base.getPortProxy()))).build()
                                        : HttpClient.newHttpClient();

                                String url = String.format("%s/BaoCaoVPCP?isUrltest=1", base.getContent());

                                List<GuiBaoCaoNhiemVuRequestDTO> requestDTOS = missionHisService.getBodyGuiBaoCao(dataSource);
                                if (CollectionUtils.isEmpty(requestDTOS)) return;
                                for (var requestDTO : requestDTOS) {
                                    requestDTO.setSessionId(missionRequest.getSessionId());
                                    HttpRequest request = HttpRequest.newBuilder()
                                            .uri(new URI(url))
                                            .header("Content-Type", "application/json")
                                            .header("Authorization", "Bearer " + base.getToken())
                                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestDTO), StandardCharsets.UTF_8))
                                            .build();
                                    HttpResponse<String> responseEntity = client.send(request, HttpResponse.BodyHandlers.ofString());
                                    if (responseEntity.statusCode() == HttpStatus.OK.value()) {
                                        InteractResponseDTO response = objectMapper.readValue(responseEntity.body(), InteractResponseDTO.class);
                                        if (Objects.nonNull(response)) {
                                            MessData messData = objectMapper.readValue(response.getData(), MessData.class);
                                            if (response.getMess().getMessCode() == Constants.MessCode.SUCCESS && messData.getStatus() == HttpStatus.OK.value()) {
                                                var missionHis = missionHisService.findById(dataSource, requestDTO.getData().getId());
                                                missionHis.setIsSent(1L);
                                                missionHisService.saveOrUpdate(dataSource, missionHis);
                                                logService.log(BAO_CAO, StringUtils.merge(missionHis.getCode(), messData.getMessage()), base.getUrl(), false);
                                                log.info(String.format("%s - %s - %s", requestDTO.getData().getMaNhiemVu(), messData.getError(), messData.getMessage()));
                                            } else {
                                                var missionHis = missionHisService.findById(dataSource, requestDTO.getData().getId());
                                                missionHis.setIsSent(1L);
                                                missionHis.setMessageLog(messData.getMessage());
                                                missionHisService.saveOrUpdate(dataSource, missionHis);
                                                logService.log(BAO_CAO, StringUtils.merge(missionHis.getCode(), messData.getMessage(), messData.getError()), base.getUrl(), true);
                                                log.error(String.format("%s - %s - %s", requestDTO.getData().getMaNhiemVu(), messData.getError(), messData.getMessage()));
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        }



                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                });

    }

    @Scheduled(fixedDelay = 10 * 1000)
    public void runSyncProgressVPCP() {

        var syncFlowStatics = staticFlowService.findAll();
        if (CollectionUtils.isEmpty(syncFlowStatics)) {
            return;
        }

        Flux.fromIterable(syncFlowStatics)
                .subscribe(base -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        MissionRequest missionRequest = new MissionRequest();
                        if (Objects.nonNull(base.getPayload()) && !base.getPayload().isBlank()) {
                            missionRequest = objectMapper.readValue(base.getPayload(), MissionRequest.class);
                        }
                        DataSource dataSources = ClientFactory.getDataSource(base.getDriver(), base.getUrl(), base.getUserName(), base.getPassword());
                        if (dataSources instanceof HikariDataSource dataSource) {
                            try (dataSource) {
                                HttpClient client = checkProxy(base.getHostProxy(), base.getPortProxy())
                                        ? HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(base.getHostProxy(), base.getPortProxy()))).build()
                                        : HttpClient.newHttpClient();

                                String url = getUrlAPI(base.getContent(), "TienDoXuLyNhiemVu", true);

                                List<TienDoXuLyNhiemVuRequestDTO> requestDTOS = missionHisService.getBodyTienDoXuLy(dataSource, base.getSaveFilePath());
                                if (CollectionUtils.isEmpty(requestDTOS)) return;
                                for (var requestDTO : requestDTOS) {
                                    requestDTO.setSessionId(missionRequest.getSessionId());
                                    HttpRequest request = HttpRequest.newBuilder()
                                            .uri(new URI(url))
                                            .header("Content-Type", "application/json")
                                            .header("Authorization", "Bearer " + base.getToken())
                                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestDTO), StandardCharsets.UTF_8))
                                            .build();
                                    HttpResponse<String> responseEntity = client.send(request, HttpResponse.BodyHandlers.ofString());
                                    if (responseEntity.statusCode() == HttpStatus.OK.value()) {
                                        InteractResponseDTO response = objectMapper.readValue(responseEntity.body(), InteractResponseDTO.class);
                                        if (Objects.nonNull(response)) {
                                            MessData messData = objectMapper.readValue(response.getData(), MessData.class);
                                            if (response.getMess().getMessCode() == Constants.MessCode.SUCCESS && messData.getStatus() == HttpStatus.OK.value()) {
                                                var missionHis = missionHisService.findById(dataSource, requestDTO.getData().getId());
                                                if (requestDTO.getData().getTrangThaiXuLy().equals(3L)) {
                                                    missionHis.setType(3L);
                                                } else {
                                                    missionHis.setIsSent(1L);
                                                }
                                                missionHisService.saveOrUpdate(dataSource, missionHis);
                                                logService.log(CAP_NHAT_TIEN_DO, StringUtils.merge(missionHis.getCode(), messData.getMessage()), base.getUrl(), false);
                                                log.info(String.format("%s - %s - %s", requestDTO.getData().getMaNhiemVu(), messData.getError(), messData.getMessage()));
                                            } else {
                                                var missionHis = missionHisService.findById(dataSource, requestDTO.getData().getId());
                                                missionHis.setIsSent(1L);
                                                missionHis.setMessageLog(messData.getMessage());
                                                missionHisService.saveOrUpdate(dataSource, missionHis);
                                                logService.log(CAP_NHAT_TIEN_DO, StringUtils.merge(missionHis.getCode(), messData.getMessage(), messData.getError()), base.getUrl(), true);
                                                log.error(String.format("%s - %s - %s", requestDTO.getData().getMaNhiemVu(), messData.getError(), messData.getMessage()));
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        }

                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                });



    }

    @Scheduled(fixedDelay = 10 * 1000)
    public void runSyncAdjustVPCP() {
        var syncFlowStatics = staticFlowService.findAll();
        if (CollectionUtils.isEmpty(syncFlowStatics)) {
            return;
        }

        Flux.fromIterable(syncFlowStatics)
                .subscribe(base -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        MissionRequest missionRequest = new MissionRequest();
                        if (Objects.nonNull(base.getPayload()) && !base.getPayload().isBlank()) {
                            missionRequest = objectMapper.readValue(base.getPayload(), MissionRequest.class);
                        }
                        DataSource dataSources = ClientFactory.getDataSource(base.getDriver(), base.getUrl(), base.getUserName(), base.getPassword());

                        if (dataSources instanceof HikariDataSource dataSource) {
                            try (dataSource) {
                                HttpClient client = checkProxy(base.getHostProxy(), base.getPortProxy())
                                        ? HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(base.getHostProxy(), base.getPortProxy()))).build()
                                        : HttpClient.newHttpClient();

                                String url = getUrlAPI(base.getContent(), "DieuChinhThoiHan", true);

                                List<DieuChinhThoiGianNhiemVuDTO> requestDTOS = missionHisService.getBodyDieuChinhThoiGianNhiemVu(dataSource, base.getSaveFilePath());
                                if (CollectionUtils.isEmpty(requestDTOS)) return;
                                for (var requestDTO : requestDTOS) {
                                    requestDTO.setSessionId(missionRequest.getSessionId());
                                    HttpRequest request = HttpRequest.newBuilder()
                                            .uri(new URI(url))
                                            .header("Content-Type", "application/json")
                                            .header("Authorization", "Bearer " + base.getToken())
                                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestDTO), StandardCharsets.UTF_8))
                                            .build();
                                    HttpResponse<String> responseEntity = client.send(request, HttpResponse.BodyHandlers.ofString());
                                    if (responseEntity.statusCode() == HttpStatus.OK.value()) {
                                        InteractResponseDTO response = objectMapper.readValue(responseEntity.body(), InteractResponseDTO.class);
                                        if (Objects.nonNull(response)) {
                                            MessData messData = objectMapper.readValue(response.getData(), MessData.class);
                                            if (response.getMess().getMessCode() == Constants.MessCode.SUCCESS && messData.getStatus() == HttpStatus.OK.value()) {
                                                var missionHis = missionHisService.findById(dataSource, requestDTO.getData().getId());
                                                missionHis.setIsSent(1L);
                                                missionHisService.saveOrUpdate(dataSource, missionHis);
                                                logService.log(DIEU_CHINH_THOI_GIAN, StringUtils.merge(missionHis.getCode(), messData.getMessage()), base.getUrl(), false);
                                                log.info(String.format("%s - %s - %s", requestDTO.getData().getMaNhiemVu(), messData.getError(), messData.getMessage()));
                                            } else {
                                                var missionHis = missionHisService.findById(dataSource, requestDTO.getData().getId());
                                                missionHis.setIsSent(1L);
                                                missionHis.setMessageLog(messData.getMessage());
                                                missionHisService.saveOrUpdate(dataSource, missionHis);
                                                logService.log(DIEU_CHINH_THOI_GIAN, StringUtils.merge(missionHis.getCode(), messData.getMessage(), messData.getError()), base.getUrl(), true);
                                                log.error(String.format("%s - %s - %s", requestDTO.getData().getMaNhiemVu(), messData.getError(), messData.getMessage()));
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        }


                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                });

    }

    private boolean checkProxy(String url, Integer port) {
        return Objects.nonNull(url) && Objects.nonNull(port) && !url.isBlank();
    }

    private ChiTietNhiemVuGiaoResponseDTO getChiTietNhiemVuGiaoFromApi(String maNhiemVu, SyncFlowStaticQuery base, String sessionId) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String url = getUrlAPI(base.getContent(), "ChiTietNhiemVuGiao", true);
            ChiTietNhiemVuGiaoResponseDTO results = null;
            ChiTietNhiemVuGiaoRequestDTO requestDTO = new ChiTietNhiemVuGiaoRequestDTO();
            requestDTO.setSessionId(sessionId);
            requestDTO.setData(ChiTietNhiemVuGiaoRequestDTO.Data.builder()
                    .maNhiemVu(maNhiemVu)
                    .build());
            HttpClient client = checkProxy(base.getHostProxy(), base.getPortProxy())
                    ? HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(base.getHostProxy(), base.getPortProxy()))).build()
                    : HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + base.getToken())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestDTO), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> responseEntity = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (responseEntity.statusCode() == HttpStatus.OK.value()) {
                ResponseDTO response = objectMapper.readValue(responseEntity.body(), ResponseDTO.class);
                if (Objects.nonNull(response) && response.getMess().getMessCode() == Constants.MessCode.SUCCESS) {
                    results = objectMapper.readValue(response.getData(), ChiTietNhiemVuGiaoResponseDTO.class);
                }
            }
            return results;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    private String getFileDinhKemFromApi(EdocReceiveFileAttachEntity entity, SyncFlowStaticQuery base, String sessionId) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String url = getUrlAPI(base.getContent(), "TaiFileDinhKem", true);
            String results = null;
            TaiFileDinhKemRequestDTO requestDTO = new TaiFileDinhKemRequestDTO();
            requestDTO.setSessionId(sessionId);
            requestDTO.setData(TaiFileDinhKemRequestDTO.Data.builder()
                    .IDFileDinhKem(entity.getFileId())
                    .TypeId(String.valueOf(entity.getTypeId()))
                    .build());
            HttpClient client = checkProxy(base.getHostProxy(), base.getPortProxy())
                    ? HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(base.getHostProxy(), base.getPortProxy()))).build()
                    : HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + base.getToken())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestDTO), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> responseEntity = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (responseEntity.statusCode() == HttpStatus.OK.value()) {
                results = responseEntity.body();
                // results = "JVBERi0xLjUKJeLjz9MKMyAwIG9iago8PC9MZW5ndGgxIDI2ODI4L0ZpbHRlci9GbGF0ZURlY29kZS9MZW5ndGggMTI4ODU+PnN0cmVhbQp4nO18eWBT1bb32sM5SdqkTTqmA81J0wRogEJbhpZKU2gZRObBFqkUKDJUZCiTyFBkKBSQihcEVCZRJpFQCpZBqYpwQREUwQEEVFDUi1QvotAh39onaQWu99373vfeH9/7yOnvrD2sPa291tprh1IgAOAHRcBAGT526PjroedfxpLXAIIuDJ8ySVk5/uMpAMFLAOSWj40fOXZq462/AkQcBJDcIx9/8rGdO9+yAFgeBwjNHzViaP7RLj/2BEg+hX20GYUFQWNNywBaazEfN2rspGkPv/FEPuabAYSkPj5u+FDIm18F0G0U5jPHDp02XrdX/xxALo4PyviJI8b/uGPC15j/ACdZi2XMh2gQ8wbtbcwRsQDg2gsAxIEZDThgFplNlpMNxE3OEw89So/RLxlhjOmYjc1iJWwJ28A+5Hreiw/mQ/iz0j7pXekH6Yasl6Nki5wq95UL5LHyBHmWvEBeJW+St8o75Q/kM/KtmPkxt5RAJVSJUWIVh9JCaakkKalKmtJByVTGKbOVTcpm5TWrZA22hlljrQ5rC2t/66PWFdYtsTRWjg2MDYoNjY2MtcQ2jXXGdo0dGjvCRm1Gm9UOdmrX2432ELvZHm2PszezJ9vT7I/bi+zz7AvtS+zP2TfYX7OX2ffbD9oP29+3n7R/bv/WkeZwOTo68hzDHY85Cq5KV81XU6toVatqWq1Ut6lOq+5QnVGdWd2rOqd6ZvXi6hXVtTXDatNrf6mr8dR4PELGsB7ltJ7sJCfIbXoE5fQZgwY5zUM5PcNe5oQH8D78UV4qua";
            }
            return results;
        } catch (Exception ex) {
            // log.error(ex.getMessage(), ex);
        }
        return null;
    }

    public String checkValidateFieldMission(Object obj) throws IllegalAccessException {
        String[] arrFieldLong = MISSION_LONG.split(Constants.Pattern.REGEX_SPLIT_IMPORT);
        String[] arrFieldDate = MISSION_DATE.split(Constants.Pattern.REGEX_SPLIT_IMPORT);
        String[] arrFieldObject = MISSION_OBJECT.split(Constants.Pattern.REGEX_SPLIT_IMPORT);
        String[] arrFieldList = MISSION_LIST.split(Constants.Pattern.REGEX_SPLIT_IMPORT);
        String error = "";
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);
            String fieldName = field.getName();
            if (Objects.nonNull(value)) {
                for (String item : arrFieldLong) {
                    if (item.equals(fieldName)) {
                        if (Objects.isNull(CommonUtils.parseStringToLong(value.toString()))) {
                            error += fieldName + " " + CommonUtils.getValueFileMess("ERROR_DATA_NOT_NUMBER");
                        }
                    }
                }

                for (String item : arrFieldDate) {
                    String[] splits = item.split("-");
                    if (splits[0].equals(fieldName)) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(splits[1]);
                        try {
                            simpleDateFormat.parse((String) value);
                        } catch (ParseException ex) {
                            error += fieldName + " " + CommonUtils.getValueFileMess("ERROR_INVALID_DATE");
                        }
                    }
                }
            }

            for (String item : arrFieldObject) {
                if (item.equals(fieldName)) {
                    error += checkValidateFieldMission(value);
                }
            }

            for (String item : arrFieldList) {
                if (item.equals(fieldName)) {
                    for (Object ob : (List<?>) value) {
                        error += checkValidateFieldMission(ob);
                    }
                }
            }
        }
        return error;
    }

    private String getUrlAPI(String base, String slug, Boolean isTest) {
        if (isTest) {
            return String.format("%s/%s?isUrltest=1", base, slug);
        } else {
            return String.format("%s/%s", base, slug);
        }
    }
}
