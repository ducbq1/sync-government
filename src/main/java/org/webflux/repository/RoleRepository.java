package org.webflux.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.webflux.domain.Role;

import java.util.Optional;

@Primary
@Repository("roleRepository")
public interface RoleRepository {
    Optional<Role> findByName(String name);
    void save(Role role);
}
