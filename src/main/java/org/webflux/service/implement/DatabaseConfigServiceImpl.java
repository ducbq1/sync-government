package org.webflux.service.implement;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.webflux.config.PBKDF2Encoder;
import org.webflux.domain.Role;
import org.webflux.domain.User;
import org.webflux.domain.UserDetailsImpl;
import org.webflux.repository.DatabaseConfigRepository;
import org.webflux.repository.UserRepository;
import org.webflux.repository.query.DatabaseConfigQuery;
import org.webflux.service.DatabaseConfigService;
import org.webflux.service.UserService;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Builder
public class DatabaseConfigServiceImpl implements DatabaseConfigService {

    private final DatabaseConfigRepository repository;

    public Optional<DatabaseConfigQuery> findById(Long id) {
        return repository.findById(id);
    }
    public Optional<List<DatabaseConfigQuery>> findAll() {
        return repository.findAll();
    }

    @Override
    public void optimizeDatabase() {
        repository.optimizeDatabase();
    }
}