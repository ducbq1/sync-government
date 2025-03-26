package org.webflux.service.implement;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.webflux.domain.AuditLog;
import org.webflux.domain.UserDetailsImpl;
import org.webflux.model.AuditLogResponse;
import org.webflux.repository.LogRepository;
import org.webflux.service.AuditLogService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);
    private static final String LOG_DIR = "logs";
    private final DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final LogRepository logRepository;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private final String streamDate = sdf.format(new Date());

    public AuditLogServiceImpl(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public Flux<String> streamLogFile(String date) {
        String filename = !date.equals(streamDate) ? String.format("sync-app.%s.log", date) : "sync-app.log";
        Path logFilePath = Paths.get(LOG_DIR, filename);

        if (!Files.exists(logFilePath) || !Files.isRegularFile(logFilePath)) {
            return Flux.error(new RuntimeException("Log file not found"));
        }

        Executors.newSingleThreadExecutor().submit(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                logFilePath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException ex) {
                        logger.error("WatchService interrupted", ex);
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path changed = ev.context();

                        if (changed.endsWith(filename)) {
                            // File has been modified, read the new content
                            readLogContent(logFilePath);
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (IOException ex) {
                logger.error("Error watching log file", ex);
                sink.tryEmitError(ex);
            }
        });

        // Initial read
        readLogContent(logFilePath);

        return sink.asFlux();
    }

    private void readLogContent(Path logFilePath) {
        var sub = DataBufferUtils.read(logFilePath, dataBufferFactory, 4096)
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .subscribe(
                        sink::tryEmitNext,
                        error -> logger.error("Error reading log file", error)
                );
    }

    public List<AuditLog> findAll() {
        return logRepository.findAll();
    }

    public AuditLogResponse findAll(int page, int pageSize) {
        return logRepository.findAll(page, pageSize);
    }

    public AuditLogResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize) {
        return logRepository.findAllOrderBy(sortColumn, sortType, page, pageSize);
    }

    public Optional<AuditLog> findById(int id) {
        return logRepository.findById(id);
    }

    public AuditLogResponse search(String type, String action, String content, String detail, String createDateFrom, String createDateTo) {
        return logRepository.search(type, action, content, detail, createDateFrom, createDateTo);
    }

    public void log(AuditLog auditLog) {
        logRepository.log(auditLog);
    }

    public void log(String action, String content, String detail) {
        log(action, content, detail, true);
    }

    public void log(String action, String content, String detail, Boolean isError) {
        logRepository.log(action, content, detail, 0L, isError);
    }
}
