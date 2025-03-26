package org.webflux.web.controller.rest;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.webflux.enumerate.Status;
import org.webflux.model.AuditLogResponse;
import org.webflux.model.StaticDestinationResponse;
import org.webflux.model.Toast;
import org.webflux.service.AuditLogService;
import reactor.core.publisher.Flux;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;

@Log4j2
@RestController
@RequestMapping("/api/log-management")
public class LogRestController {
    private final AuditLogService logService;
    private final DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private volatile String streamDate = sdf.format(new Date());
    private volatile Boolean isTaken = true;

    @Autowired
    public LogRestController(AuditLogService logService) {
        this.logService = logService;
    }

    @PostMapping(value = "/{date}")
    public ResponseEntity<Toast> updateDate(@PathVariable String date) {
        this.streamDate = date;
        this.isTaken = false;
        return ResponseEntity.ok(new Toast(Status.NoMessage));
    }

    @PostMapping(value = "/reactive")
    public ResponseEntity<Toast> reactive() {
        this.isTaken = true;
        return ResponseEntity.ok(new Toast(Status.NoMessage));
    }

    @GetMapping(value = "/current-date")
    public ResponseEntity<Toast> getCurrentDate() {
        return ResponseEntity.ok(new Toast(Status.NoMessage, streamDate));
    }

    @GetMapping(value = "/audit-log")
    public ResponseEntity<Toast> getAll(@Nullable @RequestParam("sortColumn") String sortColumn,
                                        @Nullable @RequestParam("sortType") String sortType,
                                        @RequestParam(name = "page", defaultValue = "1") int page,
                                        @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

        if (Objects.isNull(sortColumn) || Objects.isNull(sortType)) {
            AuditLogResponse response = logService.findAll(page, pageSize);
            return ResponseEntity.ok(new Toast(Status.NoMessage, response));
        } else {
            AuditLogResponse response = logService.findAllOrderBy(sortColumn, sortType, page, pageSize);
            return ResponseEntity.ok(new Toast(Status.NoMessage, response));
        }
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> streamLogFile() {
        return logService.streamLogFile(streamDate);
    }

    @GetMapping(value = "/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamLog() throws IOException {
        String filename = !streamDate.equals(sdf.format(new Date())) ? String.format("sync-app.%s.log", streamDate) : "sync-app.log";
        Path logFilePath = Paths.get("logs", filename);
//        Path logFilePath = Paths.get("logs/sync-app.log");
        return DataBufferUtils.read(logFilePath, dataBufferFactory, 4096 * 10)
                .map(this::toString)
//                .flatMap(content -> Flux.fromStream(content.lines()))
                .map(line -> ServerSentEvent.builder(line).build())
                .delayElements(Duration.ofSeconds(1))
                .takeWhile(x -> isTaken);
    }

    private String toString(DataBuffer buffer) {
        byte[] bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);
        DataBufferUtils.release(buffer);
        return new String(bytes);
    }
}
