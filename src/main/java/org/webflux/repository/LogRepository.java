package org.webflux.repository;

import org.webflux.domain.AuditLog;
import org.webflux.domain.Category;
import org.webflux.model.AuditLogResponse;
import org.webflux.model.CategoryResponse;
import org.webflux.service.dto.CategoryDTO;

import java.util.List;
import java.util.Optional;

public interface LogRepository {
    List<AuditLog> findAll();
    AuditLogResponse findAll(int page, int pageSize);
    AuditLogResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize);
    Optional<AuditLog> findById(int id);
    AuditLogResponse search(String type, String action, String content, String detail, String createDateFrom, String createDateTo);
    void log(AuditLog auditLog);
    void log(String action, String content, String detail, Long createBy);
    void log(String action, String content, String detail, Long createBy, Boolean isError);
}
