package org.webflux.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.webflux.config.ClientFactory;
import org.webflux.domain.Category;
import org.webflux.domain.SyncFlowStatic;
import org.webflux.domain.mission.EdocReceiveFileAttachEntity;
import org.webflux.enumerate.MapType;
import org.webflux.helper.CommonUtils;
import org.webflux.helper.Constants;
import org.webflux.helper.DatabaseUtils;
import org.webflux.model.MissionRequest;
import org.webflux.model.SyncMapType;
import org.webflux.model.SyncTable;
import org.webflux.model.SyncTableDetail;
import org.webflux.model.dto.*;
import org.webflux.repository.query.SyncFlowStaticQuery;
import org.webflux.service.*;
import org.webflux.service.CategoryService;
import org.webflux.service.dto.MapStructureDTO;
import org.webflux.service.dto.MapStructureDetailDTO;
import org.webflux.service.dto.SyncOperatorDTO;
import org.webflux.service.mission.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.transport.ProxyProvider;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.webflux.helper.Constants.MA_TINH_NAM_DINH;

@Log4j2
@Component
public class PausableTask implements Runnable {

    private final EdocReceiveMissionService edocReceiveMissionService;
    private final MissionService missionService;
    private final EdocReceiveFileAttachService edocReceiveFileAttachService;
    private final AttachsService attachsService;
    private final MissionDetailService missionDetailService;
    private final MissionHisService missionHisService;
    private final ClientFactory clientFactory;
    private final DatabaseConfigService databaseConfigService;
    private final FlowService flowService;
    private final CategoryService categoryService;
    private final StaticFlowService staticFlowService;
    private volatile boolean paused = false;
    //private final AtomicBoolean paused = new AtomicBoolean(false);
    private final Object pauseLock = new Object();

    @Value("${fundamental.app.proxy.url}")
    private String PROXY_URL;

    @Value("${fundamental.app.proxy.port}")
    private Integer PROXY_PORT;

    @Setter
    @Getter
    private int valueTest = 0;

    @Setter
    @Getter
    private Map<Long, Double> valueProgress = new HashMap<>();


    private AtomicInteger counter = new AtomicInteger(0);
    private AtomicInteger total = new AtomicInteger(0);
    private Map<String, String> mapId = Collections.emptyMap();


    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private static final String SESSION_ID = "rmCGoSh8khtbSJgOgHBFFqo4UVk";
    private static final String AUTHORIZATION = "90cff413-33d4-3a40-8e54-52c75361b9d6";

    @Value("${MISSION_LONG}")
    String MISSION_LONG;

    @Value("${MISSION_DATE}")
    String MISSION_DATE;

    @Value("${MISSION_OBJECT}")
    String MISSION_OBJECT;

    @Value("${MISSION_LIST}")
    String MISSION_LIST;

    public PausableTask(final EdocReceiveMissionService edocReceiveMissionService,
                        final MissionService missionService,
                        final EdocReceiveFileAttachService edocReceiveFileAttachService,
                        final AttachsService attachsService,
                        final MissionDetailService missionDetailService,
                        final MissionHisService missionHisService,
                        final ClientFactory clientFactory,
                        final DatabaseConfigService databaseConfigService,
                        final CategoryService categoryService,
                        final FlowService flowService,
                        final StaticFlowService staticFlowService) {
        this.clientFactory = clientFactory;
        this.categoryService = categoryService;
        this.databaseConfigService = databaseConfigService;
        this.flowService = flowService;
        this.edocReceiveMissionService = edocReceiveMissionService;
        this.missionService = missionService;
        this.edocReceiveFileAttachService = edocReceiveFileAttachService;
        this.attachsService = attachsService;
        this.missionDetailService = missionDetailService;
        this.missionHisService = missionHisService;
        this.staticFlowService = staticFlowService;
    }


    @Override
    public void run() {
        while (true) {
            synchronized (pauseLock) {
                while (paused) { // or LocalTime.now().isAfter(LocalTime.MIDNIGHT)
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            // Task logic goes here
            System.out.println("Task is running...");

            sync();

            // Sleep for demonstration purposes (simulate task execution time)
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void sync() {

        var syncFlowStatics = staticFlowService.findAll();
        if (CollectionUtils.isEmpty(syncFlowStatics)) {
            return;
        }
        /****************
         Flux.fromIterable(syncFlowStatics)
         .log()
         .parallel()
         .runOn(Schedulers.parallel())
         .flatMap(x -> fetchUrl(x))
         .subscribe();
         ****************/
        Flux.fromIterable(syncFlowStatics)
                .map(x -> fetchUrl(x, counter.getAndIncrement()))
                .subscribe();
    }

    private static ExchangeFilterFunction authorizationHeaderFilter(String authorization) {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            ClientRequest authorizedRequest = ClientRequest.from(clientRequest)
                    .header("Authorization", "Bearer " + authorization)
                    .build();
            return Mono.just(authorizedRequest);
        });
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> System.out.println(name + ": " + value)));
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse(DataSource dataSource, SyncFlowStaticQuery base, String maDonViDuocGiao, String sessionId) {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("Response Status: " + clientResponse.statusCode());
            AtomicInteger countMission = new AtomicInteger(0);
            if (clientResponse.statusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();

                clientResponse.bodyToMono(ResponseDTO.class).doOnNext(x -> {
                    try {
                        if (Objects.nonNull(x) && x.getMess().getMessCode() == Constants.MessCode.SUCCESS) {
                            NhiemVuGiaoResponseDTO nvGiao;
                            nvGiao = objectMapper.readValue(x.getData(), NhiemVuGiaoResponseDTO.class);
                            if (Objects.nonNull(nvGiao) && !CollectionUtils.isEmpty(nvGiao.getNhiemVu())) {
                                for (NhiemVuGiaoResponseDTO.NhiemVu nv : nvGiao.getNhiemVu()) {
                                    if (Objects.nonNull(nv) && checkValidateFieldMission(nv).isEmpty()) {
                                        edocReceiveMissionService.saveEdocReceiveMission(dataSource, nv);
                                        ChiTietNhiemVuGiaoResponseDTO nvChiTiet = getChiTietNhiemVuGiaoFromApi(nv.getMaNhiemVu(), SyncFlowStaticQuery.builder().build(), sessionId);
                                        if (Objects.nonNull(nvChiTiet) && HttpStatus.OK.toString().equals(nvChiTiet.getStatus())) {
                                            ChiTietNhiemVuGiaoResponseDTO.Item itemCt = nvChiTiet.getItem();
                                            if (Objects.nonNull(itemCt) && checkValidateFieldMission(itemCt).isEmpty()) {
                                                //save mission, mission detail, mission his
                                                missionService.saveMission(dataSource, nv, itemCt, maDonViDuocGiao, base.getIsGetSyncedAgain(), base.getUrl());

                                                //save edoc
                                                edocReceiveMissionService.saveEdocReceiveMissionDetail(dataSource, itemCt, maDonViDuocGiao);
                                                System.out.println(ANSI_RED + "Nhiệm vụ: " + countMission.getAndIncrement() + ANSI_RESET);
                                                valueProgress.put(base.getId(), countMission.get() * 100.0 / Integer.valueOf(nvGiao.getTongSoBanGhi()));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }).subscribe();
            }

            clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> System.out.println(name + ": " + value)));
            return Mono.just(clientResponse);
        });
    }

    private Mono<String> fetchFlatUrl(SyncFlowStaticQuery base, int frequency) throws JsonProcessingException {
        WebClient webClient = null;
        ObjectMapper objectMapper = new ObjectMapper();
        MissionRequest missionRequest = new MissionRequest();
        if (Objects.nonNull(base.getPayload()) && !base.getPayload().isBlank()) {
            missionRequest = objectMapper.readValue(base.getPayload(), MissionRequest.class);
        }

        String maDonViDuocGiao = Objects.nonNull(missionRequest.getData().getMaDonViDuocGiao()) ? missionRequest.getData().getMaDonViDuocGiao() : MA_TINH_NAM_DINH;
        DataSource dataSources = ClientFactory.getDataSource(base.getDriver(), base.getUrl(), base.getUserName(), base.getPassword());
        if (dataSources instanceof HikariDataSource dataSource) {
            try (dataSource) {
                if (checkProxy()) {
                    reactor.netty.http.client.HttpClient httpClient = reactor.netty.http.client.HttpClient.create()
                            .compress(true)
                            .followRedirect(true)
                            .proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP)
                                    .host(PROXY_URL)
                                    .port(PROXY_PORT));
                    webClient = WebClient.builder()
                            .clientConnector(new ReactorClientHttpConnector(httpClient))
                            .baseUrl(base.getContent())
                            .filter(authorizationHeaderFilter(base.getToken()))
                            .filter(logRequest())
                            .filter(logResponse(dataSource, base, maDonViDuocGiao, missionRequest.getSessionId()))
                            .build();
                } else {
                    webClient = WebClient.builder()
                            .baseUrl(base.getContent())
                            .filter(authorizationHeaderFilter(base.getToken()))
                            .filter(logRequest())
                            .filter(logResponse(dataSource, base, maDonViDuocGiao, missionRequest.getSessionId()))
                            .build();
                }


                NhiemVuGiaoRequestDTO requestDTO = new NhiemVuGiaoRequestDTO();
                requestDTO.setSessionId(SESSION_ID);
                requestDTO.setData(NhiemVuGiaoRequestDTO.Data.builder()
                        .maDonViDuocGiao(maDonViDuocGiao)
                        .tuNgay("20240101")
                        .denNgay("20240630")
                        .trangThaiCapNhat("0")
                        .build());

                webClient
                        .post()
                        .uri("/NhiemVuGiao?isUrltest=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(requestDTO), NhiemVuGiaoRequestDTO.class)
                        .retrieve()
                        .bodyToMono(String.class)
                        .subscribe();
                return Mono.empty();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return Mono.empty();
    }

    private Mono<String> fetchUrl(SyncFlowStaticQuery base, int frequency) {

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
                Date referenceDate = new Date();

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
                    AtomicInteger countMission = new AtomicInteger(1);
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
                                long sum = nvGiao.getNhiemVu().stream().count();
                                for (NhiemVuGiaoResponseDTO.NhiemVu nv : nvGiao.getNhiemVu()) {
                                    if (Objects.nonNull(nv) && checkValidateFieldMission(nv).isEmpty()) {
                                        edocReceiveMissionService.saveEdocReceiveMission(dataSource, nv);
                                        ChiTietNhiemVuGiaoResponseDTO nvChiTiet = getChiTietNhiemVuGiaoFromApi(nv.getMaNhiemVu(), base, missionRequest.getSessionId());
                                        if (Objects.nonNull(nvChiTiet) && HttpStatus.OK.value() == Integer.parseInt(nvChiTiet.getStatus())) {
                                            ChiTietNhiemVuGiaoResponseDTO.Item itemCt = nvChiTiet.getItem();
                                            if (Objects.nonNull(itemCt) && checkValidateFieldMission(itemCt).isEmpty()) {
                                                //save mission, mission detail, mission his
                                                missionService.saveMission(dataSource, nv, itemCt, maDonViDuocGiao, base.getIsGetSyncedAgain(), base.getUrl());
                                                //save edoc
                                                edocReceiveMissionService.saveEdocReceiveMissionDetail(dataSource, itemCt, maDonViDuocGiao);
                                                System.out.println(ANSI_RED + "Nhiệm vụ: " + countMission.getAndIncrement() + ANSI_RESET);
                                                valueProgress.put(base.getId(), frequency * 100 + countMission.get() * 100.0 / sum);
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
        return Mono.empty();
    }


    private void loopThroughJson(JsonNode node, Map<String, String> paramList) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (paramList.containsKey(field.getKey())) {
                    field.setValue(JsonNodeFactory.instance.textNode(paramList.get(field.getKey())));
                }
                loopThroughJson(field.getValue(), paramList);
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                loopThroughJson(item, paramList);
            }
        }
    }

    private JsonNode getJsonByPath(JsonNode node, String text) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();
                if (key == "data" && value.isTextual()) {
                    value = mapper.readTree(value.textValue());
                }
                if (text.equals(key)) {
                    return value;
                }
                if (Objects.nonNull(getJsonByPath(value, text))) {
                    return getJsonByPath(value, text);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                if (Objects.nonNull(getJsonByPath(item, text))) {
                    return getJsonByPath(item, text);
                }
            }
        }
        return null;
    }

    private boolean checkJsonContainsText(JsonNode node, String text) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (checkJsonContainsText(field.getValue(), text)) {
                    return true;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                if (checkJsonContainsText(item, text)) {
                    return true;
                }
            }
        } else if (node.isTextual()) {
            return node.asText().contains(text);
        }
        return false;
    }

    private boolean checkProxy() {
        return Strings.isNotBlank(PROXY_URL) && Objects.nonNull(PROXY_PORT);
    }

    private boolean checkProxy(String url, Integer port) {
        return Objects.nonNull(url) && Objects.nonNull(port) && !url.isBlank();
    }

    private void loopThroughJsonToFindField(
            List<SyncTable> syncTables,
            List<SyncTableDetail> syncTableDetails,
            JsonNode node,
            Map<String, String> paramList,
            Map<Long, Boolean> operatorChecked,
            Map<Long, Category> dictCategories,
            Map<Long, SyncOperatorDTO> dictOperator,
            List<MapStructureDTO> mapStructure,
            List<MapStructureDetailDTO> mapStructureDetail,
            Long operatorId, int currentDept, int requiredDept, String requirePath, List<String> listArray
    ) throws IOException, URISyntaxException, InterruptedException {
        var type = dictCategories.get(dictOperator.get(operatorId).getCategoryId());
        ObjectMapper mapper = new ObjectMapper();
        var rootNode = mapper.readTree(type.getPayload());
        if (node.isObject()) {
            var resultList = new HashMap<String, String>();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                if (key == "data" && value.isTextual()) {
                    value = mapper.readTree(value.textValue());
                }

                if (requiredDept == 0) {
                    if (paramList.containsKey(key)) {
                        resultList.put(paramList.get(key), value.asText());
                    }
                    if (resultList.isEmpty()) {
                        loopThroughJsonToFindField(syncTables, syncTableDetails, value, paramList,
                                operatorChecked,
                                dictCategories,
                                dictOperator,
                                mapStructure,
                                mapStructureDetail,
                                operatorId, currentDept++, 0, requirePath, listArray);
                    }
                } else if (currentDept == requiredDept) {
                    if (paramList.containsKey(key)) {
                        resultList.put(paramList.get(key), value.asText());
                    }
                } else if (currentDept < requiredDept) {
                    loopThroughJsonToFindField(syncTables, syncTableDetails, value, paramList,
                            operatorChecked,
                            dictCategories,
                            dictOperator,
                            mapStructure,
                            mapStructureDetail,
                            operatorId, currentDept++, 0, requirePath, listArray);
                }
            }

            var param = mapStructure.stream().filter(x -> x.getDestination().getId().equals(operatorId)).collect(Collectors.toList());
            for (var paramDetail : param) {
                var t = dictCategories.get(dictOperator.get(paramDetail.getSource().getId()).getCategoryId());
                if (t.getType().equals("SOURCE") && t.getName().equals("PARAM")) {
                    var pl = mapStructureDetail.stream().filter(x -> x.getMapStructureId().equals(paramDetail.getId())).collect(Collectors.toMap(MapStructureDetailDTO::getDestinationField, MapStructureDetailDTO::getSourceField));
                    loopThroughJson(rootNode, pl);
                } else if (t.getType().equals("TABLE")) {

                }
            }

            loopThroughJson(rootNode, resultList);


        } else if (node.isArray()) {
            for (JsonNode item : node) {
                loopThroughJsonToFindField(syncTables, syncTableDetails, item, paramList,
                        operatorChecked,
                        dictCategories,
                        dictOperator,
                        mapStructure,
                        mapStructureDetail,
                        operatorId, currentDept++, 0, requirePath, listArray);
            }
        }

        if (!checkJsonContainsText(rootNode, "string")) {
            setValueTest(counter.getAndIncrement() * 100 / total.get());
            try {

                HttpClient client = checkProxy()
                        ? HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(PROXY_URL, PROXY_PORT))).build()
                        : HttpClient.newHttpClient();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(type.getContent()))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + type.getToken())
                        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(rootNode), StandardCharsets.UTF_8))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                var responseJson = mapper.readTree(response.body());

                String parentId = new UUID(0L, 0L).toString();
                String keyArray = listArray.stream().filter(x -> {
                    try {
                        if (Objects.nonNull(getJsonByPath(rootNode, x)) && getJsonByPath(rootNode, x).isTextual()) {
                            return true;
                        }
                        return false;
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                }).map(x -> {
                    try {
                        return getJsonByPath(rootNode, x).textValue();
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                }).collect(Collectors.joining(","));
                if (mapId.containsKey(keyArray)) {
                    parentId = mapId.get(keyArray);
                }

                fetchSync(dictOperator.get(operatorId), responseJson, syncTables, syncTableDetails,
                        operatorChecked,
                        dictCategories, dictOperator, mapStructure, mapStructureDetail, 0, 0, requirePath, parentId, listArray);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    private void loopThroughJsonToGetField(List<SyncTable> syncTables,
                                           List<SyncTableDetail> syncTableDetails,
                                           JsonNode node,
                                           Map<MapType, Map<String, SyncMapType>> paramList,
                                           int currentDept, int requireDept, String requirePath, String parentId, List<String> listArray) throws JsonProcessingException {

        var normal = paramList.get(MapType.Normal);
        var func = paramList.get(MapType.Function);
        var pipe = paramList.get(MapType.Pipe);
        var tableAction = paramList.get(MapType.TableAction);
        List<String> listArrayValue = new ArrayList<>();

        Map<Long, SyncTableDetail> mapFunction = new HashMap<>();
        Map<Long, SyncTableDetail> mapTableAction = new HashMap<>();

        if (node.isObject()) {
            String syncTableId = UUID.randomUUID().toString();
            SyncTable syncTable = SyncTable.builder().id(syncTableId).parentId(parentId).build();

            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                if (key == "data" && value.isTextual()) {
                    ObjectMapper mapper = new ObjectMapper();
                    value = mapper.readTree(value.textValue());
                }

                if (listArray.contains(key)) {
                    listArrayValue.add(value.asText());
                }

                if (!listArrayValue.isEmpty()) {
                    var keyArrayValue = String.join(",", listArrayValue);
                    mapId.put(keyArrayValue, syncTableId);
                }

                if (requireDept == 0 && requirePath.isEmpty()) {

                    if (Objects.nonNull(normal) && normal.containsKey(key)) {
                        syncTableDetails.add(SyncTableDetail.builder().syncTableId(syncTableId).name(normal.get(key).getName()).value(value.asText()).build());
                    }

                    if (Objects.nonNull(pipe)) {
                        for (var pip : pipe.entrySet()) {
                            syncTableDetails.add(SyncTableDetail.builder().syncTableId(syncTableId).name(pip.getKey()).value(pip.getValue().getName()).build());
                        }
                    }

                    if (Objects.nonNull(tableAction)
                            && Strings.isNotBlank(key) && !key.isBlank()
                            && Objects.nonNull(value) && !value.asText().isBlank()
                            && tableAction.containsKey(key)) {
                        Long funcId = tableAction.get(key).getFunctionId();
                        String functionHead;
                        if (mapTableAction.containsKey(funcId)) {
                            String f = mapTableAction.get(funcId).getFunctionHead();
                            functionHead = f.replaceAll(key, "'" + value.asText() + "'");
                            mapTableAction.remove(funcId);
                        } else {
                            String f = tableAction.get(key).getFunctionBody();
                            functionHead = f.substring(f.indexOf("function") + 9, f.indexOf("{") - 1).replaceAll(key, "'" + value.asText() + "'");
                        }
                        mapTableAction.put(funcId, SyncTableDetail.builder()
                                .functionHead(functionHead)
                                .functionBody(tableAction.get(key).getFunctionBody() + "\n" + functionHead)
                                .syncTableId(syncTableId)
                                .name(tableAction.get(key).getName()).build());
                    }

                    if (Objects.nonNull(func)
                            && Strings.isNotBlank(key)
                            && Objects.nonNull(value) && !value.asText().isBlank()
                            && func.containsKey(key)) {
                        Long funcId = func.get(key).getFunctionId();
                        String functionHead;
                        if (mapFunction.containsKey(funcId)) {
                            String f = mapFunction.get(funcId).getFunctionHead();
                            functionHead = f.replaceAll(key, "'" + value.asText() + "'");
                            mapFunction.remove(funcId);
                        } else {
                            String f = func.get(key).getFunctionBody();
                            functionHead = f.substring(f.indexOf("function") + 9, f.indexOf("{") - 1).replaceAll(key, "'" + value.asText() + "'");
                        }
                        mapFunction.put(funcId, SyncTableDetail.builder()
                                .functionHead(functionHead)
                                .functionBody(func.get(key).getFunctionBody() + "\n" + functionHead)
                                .syncTableId(syncTableId)
                                .name(func.get(key).getName()).build());
                    }

                    if (syncTableDetails.size() == 0) {
                        loopThroughJsonToGetField(syncTables, syncTableDetails, value, paramList, currentDept++, requireDept, requirePath, parentId, listArray);
                    }

                } else if (requirePath.equals(key)) {
                    loopThroughJsonToGetField(syncTables, syncTableDetails, value, paramList, currentDept, 0, "", parentId, listArray);
                } else if (currentDept == requireDept) {
                    if (Objects.nonNull(normal) && normal.containsKey(key)) {
                        syncTableDetails.add(SyncTableDetail.builder().syncTableId(syncTableId).name(normal.get(key).getName()).value(value.asText()).build());
                    }

                    if (Objects.nonNull(tableAction)
                            && Strings.isNotBlank(key)
                            && Objects.nonNull(value) && !value.asText().isBlank()
                            && tableAction.containsKey(key)) {
                        Long funcId = tableAction.get(key).getFunctionId();
                        String functionHead;
                        if (mapTableAction.containsKey(funcId)) {
                            String f = mapTableAction.get(funcId).getFunctionHead();
                            functionHead = f.replaceAll(key, "'" + value.asText() + "'");
                            mapTableAction.remove(funcId);
                        } else {
                            String f = tableAction.get(key).getFunctionBody();
                            functionHead = f.substring(f.indexOf("function") + 9, f.indexOf("{") - 1).replaceAll(key, "'" + value.asText() + "'");
                        }
                        mapTableAction.put(funcId, SyncTableDetail.builder()
                                .functionHead(functionHead)
                                .functionBody(tableAction.get(key).getFunctionBody() + "\n" + functionHead)
                                .syncTableId(syncTableId)
                                .name(tableAction.get(key).getName()).build());
                    }

                    if (Objects.nonNull(func)
                            && Strings.isNotBlank(key)
                            && Objects.nonNull(value)
                            && func.containsKey(key)) {
                        Long funcId = func.get(key).getFunctionId();
                        String functionHead;
                        if (mapFunction.containsKey(funcId)) {
                            String f = mapFunction.get(funcId).getFunctionHead();
                            functionHead = f.replaceAll(key, "'" + value.asText() + "'");
                            mapFunction.remove(funcId);
                        } else {
                            String f = func.get(key).getFunctionBody() + ";";
                            functionHead = f.substring(f.indexOf("function") + 9, f.indexOf("{") - 1).replaceAll(key, "'" + value.asText() + "'");
                        }
                        mapFunction.put(funcId, SyncTableDetail.builder()
                                .functionHead(functionHead)
                                .functionBody(func.get(key).getFunctionBody() + "\n" + functionHead)
                                .syncTableId(syncTableId)
                                .name(func.get(key).getName()).build());
                    }
                } else if (currentDept < requireDept) {
                    loopThroughJsonToGetField(syncTables, syncTableDetails, value, paramList, currentDept++, requireDept, requirePath, parentId, listArray);
                }
            }
            if (syncTableDetails.size() > 0) {

                for (var map : mapFunction.entrySet()) {
                    int i = 0;
                    var listField = map.getValue().getName().split(",");
                    Engine engine = Engine.newBuilder()
                            .option("engine.WarnInterpreterOnly", "false")
                            .build();
                    try (Context context = Context.newBuilder("js").engine(engine).build()) {
                        try {
                            String[] temp = context.eval("js", map.getValue().getFunctionBody()).as(String[].class);
                            syncTableDetails.add(SyncTableDetail.builder().syncTableId(syncTableId).name(listField[i++]).value(temp[i]).build());
                        } catch (Exception ex) {
                            log.error(ex.getMessage(), ex);
                        }
                    }
                }

                for (var map : mapTableAction.entrySet()) {
                    Engine engine = Engine.newBuilder()
                            .option("engine.WarnInterpreterOnly", "false")
                            .build();
                    try (Context context = Context.newBuilder("js").engine(engine).build()) {
                        try {
                            String temp = context.eval("js", map.getValue().getFunctionBody()).asString();
                            syncTable.setAction(temp);
                        } catch (Exception ex) {
                            log.error(ex.getMessage(), ex);
                        }
                    }
                }

                syncTables.add(syncTable);
            }

        } else if (node.isArray()) {
            for (JsonNode item : node) {
                loopThroughJsonToGetField(syncTables, syncTableDetails, item, paramList, currentDept++, requireDept, requirePath, parentId, listArray);
            }
        }
    }

    private void executeFlow() {
        try {

            var categories = categoryService.findAll();
            var databaseConfigs = databaseConfigService.findAll();
            var listFlow = flowService.findAll();

            var dictCategories = categories.stream().collect(Collectors.toMap(Category::getId, x -> x));


            for (var flow : listFlow) {

                List<SyncTable> syncTables = new ArrayList<>();
                List<SyncTableDetail> syncTableDetails = new ArrayList<>();

                var operator = flowService.findAllSyncOperator(flow.getId());
                var operatorChecked = operator.stream().collect(Collectors.toMap(SyncOperatorDTO::getId, x -> false));
                var mapStructure = flowService.findAllMapStructure(flow.getId());
                var mapStructureId = mapStructure.stream().map(x -> x.getId().toString()).collect(Collectors.joining(","));
                var mapStructureDetail = flowService.findAllMapStructureDetail(mapStructureId);

                var dictOperator = operator.stream().collect(Collectors.toMap(SyncOperatorDTO::getId, x -> x));

                var sourceOperators = operator.stream().filter(x -> "SOURCE".equals(dictCategories.get(x.getCategoryId()).getType())).collect(Collectors.toList());
                var mainOperators = sourceOperators.stream().filter(x -> {
                    var beforeMap = mapStructure.stream().filter(m -> m.getDestination().getId().equals(x.getId())).map(y -> dictCategories.get(dictOperator.get(y.getSource().getId()).getCategoryId())).collect(Collectors.toList());
                    return beforeMap.size() > 0 && beforeMap.stream().allMatch(t -> (t.getType().equals("SOURCE") && t.getName().equals("PARAM")) || t.getType().equals("TABLE"));
                }).collect(Collectors.toList());
                for (var mainOperator : mainOperators) {
                    var api = dictCategories.get(mainOperator.getCategoryId());
                    ObjectMapper objectMapper = new ObjectMapper();
                    var rootNode = objectMapper.readTree(api.getPayload());

                    var param = mapStructure.stream().filter(x -> x.getDestination().getId().equals(mainOperator.getId())).collect(Collectors.toList());
                    for (var paramDetail : param) {
                        var type = dictCategories.get(dictOperator.get(paramDetail.getSource().getId()).getCategoryId());
                        if (type.getType().equals("SOURCE") && type.getName().equals("PARAM")) {
                            var paramList = mapStructureDetail.stream().filter(x -> x.getMapStructureId().equals(paramDetail.getId())).collect(Collectors.toMap(MapStructureDetailDTO::getDestinationField, MapStructureDetailDTO::getSourceField));
                            loopThroughJson(rootNode, paramList);
                            setCheckedOperator(operatorChecked, paramDetail.getSource().getId());

                        } else if (type.getType().equals("TABLE")) {

                        }
                    }

                    try {
                        HttpClient client = checkProxy()
                                ? HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(PROXY_URL, PROXY_PORT))).build()
                                : HttpClient.newHttpClient();

                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(new URI(api.getContent()))
                                .header("Content-Type", "application/json")
                                .header("Authorization", "Bearer " + api.getToken())
                                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(rootNode), StandardCharsets.UTF_8))
                                .build();
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        var responseJson = objectMapper.readTree(response.body());
                        int tongSoBanGhi = objectMapper.readTree(responseJson.path("data").asText()).path("TongSoBanGhi").asInt();
                        total.set(tongSoBanGhi);

                        fetchSync(mainOperator, responseJson, syncTables, syncTableDetails, operatorChecked,
                                dictCategories, dictOperator, mapStructure, mapStructureDetail, 0, 0, "", new UUID(0L, 0L).toString(), new ArrayList<>());


                        // Print the response
                        System.out.println("Response Code: " + response.statusCode());
                        System.out.println("Response Body: " + response.body());

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        clearCount();
                    }
                }

                for (var config : databaseConfigs.get()) {
                    var syncTableGroups = syncTables.stream().filter(x -> dictCategories.get(dictOperator.get(x.getOperatorId()).getCategoryId()).getDatabaseConfigId().equals(config.getId())).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(syncTableGroups)) {
                        JdbcTemplate database = clientFactory.getJdbcTemplate(config.getDriver(), config.getUrl(), config.getUserName(), config.getPassword());
                        var syncTableByOperators = syncTableGroups.stream().collect(groupingBy(SyncTable::getOperatorId));
                        for (var syncTableByOperator : syncTableByOperators.entrySet()) {
                            List<Map<String, Object>> temp = new ArrayList<>();
                            syncTableByOperator.getValue().forEach(y -> {
                                var fields = syncTableDetails.stream().filter(x -> x.getSyncTableId().equals(y.getId())).collect(Collectors.toMap(m -> m.getName(), m -> (Object) m.getValue()));
                                var extendFields = syncTableDetails.stream().filter(x -> (Strings.isBlank(x.getSyncTableId())) && x.getOperatorId().equals(y.getOperatorId())).collect(Collectors.toList());
                                extendFields.forEach(e -> {
                                    fields.put(e.getName(), e.getValue());
                                });
                                fields.put(y.getPrimaryKey(), y.getSequence());
                                temp.add(fields);
                            });
                            try {
                                DatabaseUtils.insert(database, syncTableByOperator.getValue().get(0).getName(), temp);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                clearCount();
                            }
                        }
                    }
                }

            }


        } catch (Exception ex) {
            ex.printStackTrace();
            clearCount();
        }
    }

    private void clearCount() {
        counter.set(0);
        total.set(0);
        setValueTest(0);
    }

    private void fetchSync(
            SyncOperatorDTO mainOperator,
            JsonNode responseJson,
            List<SyncTable> syncTables,
            List<SyncTableDetail> syncTableDetails,
            Map<Long, Boolean> operatorChecked,
            Map<Long, Category> dictCategories,
            Map<Long, SyncOperatorDTO> dictOperator,
            List<MapStructureDTO> mapStructure,
            List<MapStructureDetailDTO> mapStructureDetail,
            int currentDept, int requireDept, String requirePath, String parentId, List<String> listArray
    ) throws IOException, URISyntaxException, InterruptedException {
        var nextMaps = mapStructure.stream().filter(x -> x.getSource().getId().equals(mainOperator.getId())).collect(Collectors.toList());
        for (var nextMap : nextMaps) {
            var type = dictCategories.get(dictOperator.get(nextMap.getDestination().getId()).getCategoryId());

            if (type.getType().equals("TABLE")) {
                Map<MapType, Map<String, SyncMapType>> paramList = new HashMap<>();
                String action = "INSERT";
                var beforeMaps = mapStructure.stream().filter(x ->
                        !x.getId().equals(nextMap.getId()) &&
                                x.getDestination().getId().equals(nextMap.getDestination().getId())).collect(Collectors.toList());

                for (var beforeMap : beforeMaps) {
                    var typeBeforeOperator = dictCategories.get(dictOperator.get(beforeMap.getSource().getId()).getCategoryId());
                    if (typeBeforeOperator.getType().equals("FUNCTION") && typeBeforeOperator.getName().equals("TABLE_ACTION")) {
                        var beforeFunc = mapStructure.stream().filter(x ->
                                x.getDestination().getId().equals(beforeMap.getSource().getId())
                                        && x.getSource().getId().equals(nextMap.getSource().getId())
                        ).findFirst();

                        if (beforeFunc.isPresent()) {
                            var bodyFunc = dictOperator.get(beforeMap.getSource().getId()).getBody();
                            var paramIns = mapStructureDetail.stream().filter(x -> x.getMapStructureId().equals(beforeFunc.get().getId())).collect(Collectors.toList());
                            var paramOuts = mapStructureDetail.stream().filter(x -> x.getMapStructureId().equals(beforeMap.getId()))
                                    .map(x -> x.getDestinationField())
                                    .collect(Collectors.joining(","));
                            for (var paramIn : paramIns) {
                                bodyFunc = bodyFunc.replaceAll(paramIn.getDestinationField(), paramIn.getSourceField());
                            }
                            String finalBodyFunc = bodyFunc;
                            var mapTemp = paramIns.stream().collect(Collectors.toMap(x -> x.getSourceField(), x -> SyncMapType.builder().functionId(beforeMap.getSource().getId()).functionBody(finalBodyFunc).name(paramOuts).build()));
                            paramList.put(MapType.TableAction, mapTemp);
                        }

                    } else if (typeBeforeOperator.getType().equals("SOURCE") && typeBeforeOperator.getName().equals("PARAM")) {
                        var mapTemp = mapStructureDetail.stream().filter(x -> x.getMapStructureId().equals(beforeMap.getId())).collect(Collectors.toMap(x -> x.getDestinationField(), x -> SyncMapType.builder().name(x.getSourceField()).build()));
                        paramList.put(MapType.Pipe, mapTemp);
                    } else if (typeBeforeOperator.getType().equals("FUNCTION") && typeBeforeOperator.getName().equals("CUSTOM_FUNCTION")) {
                        var beforeFunc = mapStructure.stream().filter(x ->
                                x.getDestination().getId().equals(beforeMap.getSource().getId())
                                        && x.getSource().getId().equals(nextMap.getSource().getId())
                        ).findFirst();

                        if (Objects.nonNull(beforeFunc)) {
                            var bodyFunc = dictOperator.get(beforeMap.getSource().getId()).getBody();
                            var paramIns = mapStructureDetail.stream().filter(x -> x.getMapStructureId().equals(beforeFunc.get().getId())).collect(Collectors.toList());
                            var paramOuts = mapStructureDetail.stream().filter(x -> x.getMapStructureId().equals(beforeMap.getId()))
                                    .map(x -> x.getDestinationField())
                                    .collect(Collectors.joining(","));
                            for (var paramIn : paramIns) {
                                bodyFunc = bodyFunc.replaceAll(paramIn.getDestinationField(), paramIn.getSourceField());
                            }
                            String finalBodyFunc = bodyFunc;
                            var mapTemp = paramIns.stream().collect(Collectors.toMap(x -> x.getSourceField(), x -> SyncMapType.builder().functionId(beforeMap.getSource().getId()).functionBody(finalBodyFunc).name(paramOuts).build()));
                            paramList.put(MapType.Function, mapTemp);
                        }
                    } else if (typeBeforeOperator.getType().equals("FUNCTION") && typeBeforeOperator.getName().equals("PIPE_FUNCTION")) {
                        var beforeFunc = mapStructure.stream().filter(x ->
                                x.getDestination().getId().equals(beforeMap.getSource().getId())
                                        && x.getSource().getId().equals(nextMap.getSource().getId())
                        ).findFirst();

                        if (Objects.nonNull(beforeFunc)) {
                            var mapTemp = mapStructureDetail.stream().filter(x -> x.getMapStructureId().equals(beforeMap.getId())).collect(Collectors.toMap(x -> x.getDestinationField(), x -> SyncMapType.builder().name(x.getSourceField()).build()));
                            paramList.put(MapType.Pipe, mapTemp);
                        }
                    }
                }

                var mapNormal = mapStructureDetail.stream().filter(x -> x.getMapStructureId().equals(nextMap.getId()))
                        .collect(Collectors.toMap(x -> x.getSourceField(), x -> SyncMapType.builder().name(x.getDestinationField()).build()));
                paramList.put(MapType.Normal, mapNormal);

                var apiNext = nextMaps.stream().filter(x -> {
                    var nextType = dictCategories.get(dictOperator.get(x.getDestination().getId()).getCategoryId());
                    if (nextType.getType().equals("SOURCE")
                            && Strings.isNotBlank(nextType.getPayload())
                            && Objects.nonNull(nextType.getName())
                            && !nextType.getName().startsWith("API_PARTIAL_")
                            && nextType.getName().startsWith("API_")) {
                        return true;
                    }
                    return false;
                }).findFirst().orElse(null);

                if (Objects.nonNull(apiNext)) {
                    listArray = mapStructureDetail.stream().filter(x -> x.getMapStructureId().equals(apiNext.getId())).map(x -> x.getSourceField()).collect(Collectors.toList());
                }

                List<SyncTable> nextSyncTables = new ArrayList<>();
                List<SyncTableDetail> nextSyncTableDetails = new ArrayList<>();
                loopThroughJsonToGetField(nextSyncTables, nextSyncTableDetails, responseJson, paramList, currentDept, 0, requirePath, parentId, listArray);
                for (var nextSyncTable : nextSyncTables) {
                    if (Strings.isBlank(nextSyncTable.getAction()) || nextSyncTable.getAction().equals("NULL")) {
                        nextSyncTable.setAction(action);
                    }
                    nextSyncTable.setName(type.getContent());
                    nextSyncTable.setSequence(type.getSequence());
                    nextSyncTable.setUniqueKey(type.getUniqueKey());
                    nextSyncTable.setPrimaryKey(type.getPrimaryKey());
                    nextSyncTable.setOperatorId(nextMap.getDestination().getId());
                }
                for (var nextSyncTableDetail : nextSyncTableDetails) {
                    nextSyncTableDetail.setOperatorId(nextMap.getDestination().getId());
                }
                syncTables.addAll(nextSyncTables);
                syncTableDetails.addAll(nextSyncTableDetails);
                setCheckedOperator(operatorChecked, nextMap.getDestination().getId());


            } else if (type.getType().equals("FUNCTION") && type.getName().equals("CUSTOM_FUNCTION")) {
                if (!isCheckedOperator(operatorChecked, nextMap.getDestination().getId())) {
                    setCheckedOperator(operatorChecked, nextMap.getDestination().getId());
                }
            } else if (type.getType().equals("SOURCE")
                    && Strings.isNotBlank(type.getPayload())
                    && Objects.nonNull(type.getName())
                    && !type.getName().startsWith("API_PARTIAL_")
                    && type.getName().startsWith("API_")) {
                var paramList = mapStructureDetail.stream().filter(x -> x.getMapStructureId().equals(nextMap.getId())).collect(Collectors.toMap(MapStructureDetailDTO::getSourceField, MapStructureDetailDTO::getDestinationField));

                listArray = paramList.keySet().stream().toList();

                List<SyncTable> nextSyncTables = new ArrayList<>();
                List<SyncTableDetail> nextSyncTableDetails = new ArrayList<>();
                loopThroughJsonToFindField(nextSyncTables, nextSyncTableDetails, responseJson, paramList,
                        operatorChecked,
                        dictCategories,
                        dictOperator,
                        mapStructure,
                        mapStructureDetail,
                        nextMap.getDestination().getId(), 0, 0, requirePath, listArray);
                syncTables.addAll(nextSyncTables);
                syncTableDetails.addAll(nextSyncTableDetails);
                setCheckedOperator(operatorChecked, nextMap.getDestination().getId());
            } else if (type.getType().equals("SOURCE") && type.getName().startsWith("API_PARTIAL_")) {
                String requiredPath = mapStructureDetail.stream().filter(x -> x.getMapStructureId().equals(nextMap.getId())).findFirst().get().getSourceField();
                var currentOperator = dictOperator.get(nextMap.getDestination().getId());
                var json = getJsonByPath(responseJson, requiredPath);
                if (Objects.nonNull(json) && !json.isEmpty() && !json.isNull() && false) {
                    fetchSync(currentOperator, json, syncTables, syncTableDetails, operatorChecked,
                            dictCategories, dictOperator, mapStructure, mapStructureDetail, 0, 0, requiredPath, parentId, listArray);
                }
            }
        }
    }

    private boolean isCheckedOperator(Map<Long, Boolean> operatorChecked, Long key) {
        return operatorChecked.containsKey(key) && operatorChecked.get(key);
    }

    private void setCheckedOperator(Map<Long, Boolean> operatorChecked, Long key) {
        operatorChecked.put(key, true);
    }

    public void pause() {
        synchronized (pauseLock) {
            paused = true;
        }
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    private ChiTietNhiemVuGiaoResponseDTO getChiTietNhiemVuGiaoFromApi(String maNhiemVu, SyncFlowStaticQuery base, String sessionId) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String url = String.format("%s/ChiTietNhiemVuGiao?isUrltest=1", base.getContent());
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

    private String getFileDinhKemFromApi(EdocReceiveFileAttachEntity entity) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String url = "https://api.namdinh.gov.vn/apitdnv/TaiFileDinhKem?isUrltest=1";
            String results = null;
            TaiFileDinhKemRequestDTO requestDTO = new TaiFileDinhKemRequestDTO();
            requestDTO.setSessionId(SESSION_ID);
            requestDTO.setData(TaiFileDinhKemRequestDTO.Data.builder()
                    .IDFileDinhKem(entity.getFileId())
                    .TypeId(String.valueOf(entity.getTypeId()))
                    .build());
            HttpClient client = HttpClient
                    .newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + AUTHORIZATION)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestDTO), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> responseEntity = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (responseEntity.statusCode() == HttpStatus.OK.value()) {
                results = "JVBERi0xLjUKJeLjz9MKMyAwIG9iago8PC9MZW5ndGgxIDI2ODI4L0ZpbHRlci9GbGF0ZURlY29kZS9MZW5ndGggMTI4ODU+PnN0cmVhbQp4nO18eWBT1bb32sM5SdqkTTqmA81J0wRogEJbhpZKU2gZRObBFqkUKDJUZCiTyFBkKBSQihcEVCZRJpFQCpZBqYpwQREUwQEEVFDUi1QvotAh39onaQWu99373vfeH9/7yOnvrD2sPa291tprh1IgAOAHRcBAGT526PjroedfxpLXAIIuDJ8ySVk5/uMpAMFLAOSWj40fOXZq462/AkQcBJDcIx9/8rGdO9+yAFgeBwjNHzViaP7RLj/2BEg+hX20GYUFQWNNywBaazEfN2rspGkPv/FEPuabAYSkPj5u+FDIm18F0G0U5jPHDp02XrdX/xxALo4PyviJI8b/uGPC15j/ACdZi2XMh2gQ8wbtbcwRsQDg2gsAxIEZDThgFplNlpMNxE3OEw89So/RLxlhjOmYjc1iJWwJ28A+5Hreiw/mQ/iz0j7pXekH6Yasl6Nki5wq95UL5LHyBHmWvEBeJW+St8o75Q/kM/KtmPkxt5RAJVSJUWIVh9JCaakkKalKmtJByVTGKbOVTcpm5TWrZA22hlljrQ5rC2t/66PWFdYtsTRWjg2MDYoNjY2MtcQ2jXXGdo0dGjvCRm1Gm9UOdmrX2432ELvZHm2PszezJ9vT7I/bi+zz7AvtS+zP2TfYX7OX2ffbD9oP29+3n7R/bv/WkeZwOTo68hzDHY85Cq5KV81XU6toVatqWq1Ut6lOq+5QnVGdWd2rOqd6ZvXi6hXVtTXDatNrf6mr8dR4PELGsB7ltJ7sJCfIbXoE5fQZgwY5zUM5PcNe5oQH8D78UV4qua";
            }
            return results;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
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
                    if (Objects.nonNull(value)) {
                        for (Object ob : (List<?>) value) {
                            error += checkValidateFieldMission(ob);
                        }
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
