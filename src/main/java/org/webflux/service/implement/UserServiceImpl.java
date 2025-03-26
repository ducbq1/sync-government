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
import org.webflux.repository.UserRepository;
import org.webflux.service.UserService;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Builder
public class UserServiceImpl implements UserService, ReactiveUserDetailsService {

    private final UserRepository repository;
    private final PBKDF2Encoder passwordEncoder;

    public void save(User user, List<Role> roles) {
        repository.save(user, roles);
    }

    public User findByUserName(String username) {
        Optional<User> userInfo = repository.findByUserName(username);
        return userInfo.orElseThrow(() -> new UsernameNotFoundException("user not found " + username));
    }

    public boolean existsByUserName(String userName) {
        return repository.existsByUserName(userName);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        Optional<User> userInfo = repository.findByUserName(username);
        return Mono.just(userInfo.map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("user not found " + username)));
    }

}