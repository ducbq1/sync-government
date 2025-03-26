package org.webflux.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webflux.domain.Role;

import java.util.Optional;

@Service
public interface RoleService {
    @Transactional
    Optional<Role> findByName(String name);
    void save(Role role);
}