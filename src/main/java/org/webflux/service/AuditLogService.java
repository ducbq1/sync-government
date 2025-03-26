package org.webflux.service;

import org.springframework.stereotype.Service;
import org.webflux.domain.AuditLog;
import org.webflux.model.AuditLogResponse;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

public interface AuditLogService {
    Flux<String> streamLogFile(String date);
    List<AuditLog> findAll();
    AuditLogResponse findAll(int page, int pageSize);
    AuditLogResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize);
    Optional<AuditLog> findById(int id);
    AuditLogResponse search(String type, String action, String content, String detail, String createDateFrom, String createDateTo);
    void log(AuditLog auditLog);
    void log(String action, String content, String detail);
    void log(String action, String content, String detail, Boolean isError);
}
