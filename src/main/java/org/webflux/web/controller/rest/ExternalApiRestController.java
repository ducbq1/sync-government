package org.webflux.web.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.webflux.enumerate.Status;
import org.webflux.model.ApiResponse;
import org.webflux.model.Toast;
import org.webflux.model.dto.ChiTietNhiemVuGiaoRequestDTO;
import org.webflux.model.dto.ChiTietNhiemVuGiaoResponseDTO;
import org.webflux.model.dto.NhiemVuGiaoRequestDTO;
import org.webflux.model.dto.NhiemVuGiaoResponseDTO;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Log4j2
@RestController
@RequiredArgsConstructor
public class ExternalApiRestController {

    @PostMapping(value = "/api/external-api/**")
    public Mono<ApiResponse<? extends Object>> clearToast(WebSession session, ServerWebExchange serverWebExchange) {
        String path = serverWebExchange.getRequest().getPath().toString();
        session.getAttributes().remove("toast");
        return switch (path) {
            case "/api/external-api/NhiemVuGiao" -> serverWebExchange.getRequest().getBody()
                    .switchIfEmpty(Mono.error(new IllegalStateException("Request body is missing")))
                    .next()
                    .flatMap(buffer -> {
                        byte[] bytes = new byte[buffer.readableByteCount()];
                        buffer.read(bytes);
                        DataBufferUtils.release(buffer);
                        String bodyString = new String(bytes, StandardCharsets.UTF_8);
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            var missionRequest = objectMapper.readValue(bodyString, NhiemVuGiaoRequestDTO.class);
                            var mission = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir"), "mission-fake.txt")), "UTF-8");
                            var missionResponse = objectMapper.readValue(mission, NhiemVuGiaoResponseDTO.class);
                            ApiResponse<NhiemVuGiaoResponseDTO> response = ApiResponse.success(missionResponse, "Thành công");
                            return Mono.just(response);
                        } catch (IOException e) {
                            return Mono.just(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Request processed error", null));
                        }
                    }).onErrorResume(e -> Mono.just(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null)));
            case "/api/external-api/ChiTietNhiemVuGiao" -> serverWebExchange.getRequest().getBody()
                    .switchIfEmpty(Mono.error(new IllegalStateException("Request body is missing")))
                    .next()
                    .flatMap(buffer -> {
                        byte[] bytes = new byte[buffer.readableByteCount()];
                        buffer.read(bytes);
                        DataBufferUtils.release(buffer);
                        String bodyString = new String(bytes, StandardCharsets.UTF_8);
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            var missionRequest = objectMapper.readValue(bodyString, ChiTietNhiemVuGiaoRequestDTO.class);
                            var mission = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir"), "mission-detail-fake.txt")), "UTF-8");
                            var missionResponse = objectMapper.readValue(mission, ChiTietNhiemVuGiaoResponseDTO.class);
                            ApiResponse<ChiTietNhiemVuGiaoResponseDTO> response = ApiResponse.success(missionResponse, "Thành công");
                            return Mono.just(response);
                        } catch (IOException e) {
                            return Mono.just(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Request processed error", null));
                        }
                    }).onErrorResume(e -> Mono.just(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null)));
            default ->
                    Mono.just(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Request processed error", null));
        };
    }
}
