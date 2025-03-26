package org.webflux.service.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webflux.domain.Role;
import org.webflux.repository.RoleRepository;
import org.webflux.service.RoleService;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements RoleService {
    @Qualifier("roleRepository")
    private final RoleRepository repository;

    @Transactional
    public Optional<Role> findByName(String name) {
        return repository.findByName(name);
    }

    public void save(Role role) {
        repository.save(role);
    }

}
