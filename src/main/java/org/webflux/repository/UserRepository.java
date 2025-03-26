package org.webflux.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.webflux.domain.Role;
import org.webflux.domain.User;

import java.util.List;
import java.util.Optional;

@Primary
@Repository
public interface UserRepository  {
    Optional<User> findByUserName(String username);
    Boolean existsByUserName(String username);
    void save(User user, List<Role> roles);
    Optional<User> findById(Long id);
}
