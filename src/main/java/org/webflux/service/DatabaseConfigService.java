package org.webflux.service;

import org.springframework.stereotype.Component;
import org.webflux.domain.Role;
import org.webflux.domain.User;
import org.webflux.repository.query.DatabaseConfigQuery;

import java.util.List;
import java.util.Optional;

@Component
public interface DatabaseConfigService {
    Optional<DatabaseConfigQuery> findById(Long id);
    Optional<List<DatabaseConfigQuery>> findAll();
    void optimizeDatabase();
}