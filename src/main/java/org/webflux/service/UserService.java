package org.webflux.service;

import org.springframework.stereotype.Component;
import org.webflux.domain.Role;
import org.webflux.domain.User;

import java.util.List;

@Component
public interface UserService {
    void save(User user, List<Role> roles);
    User findByUserName(String username);
    boolean existsByUserName(String userName);
}