/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2016, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package org.webflux.repository.implement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.webflux.domain.Menu;
import org.webflux.domain.Role;
import org.webflux.domain.User;
import org.webflux.enumerate.ScriptQuery;
import org.webflux.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findByUserName(String username) {
        var user = jdbcTemplate.queryForObject(ScriptQuery.getUserByName, new Object[]{username}, new int[]{Types.VARCHAR},
                (rs, rowNum) -> {
                    Long id = rs.getLong("id");
                    String userName = rs.getString("username");
                    String password = rs.getString("password");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    String image = rs.getString("image");
                    Boolean gender = rs.getInt("gender") == 1;
                    return new User(id, userName, password, firstName, lastName, image, gender);
                });
        if (Objects.nonNull(user)) {
            var role = jdbcTemplate.queryForList(ScriptQuery.getAuthByUserId, new Object[]{user.getId()}, new int[]{Types.INTEGER}, String.class);
            var menus = jdbcTemplate.query(ScriptQuery.getMenuByUserId, new Object[]{user.getId()}, new int[]{Types.INTEGER},
                    (rs, rowNum) -> {
                        Long id = rs.getLong("id");
                        String title = rs.getString("title");
                        String icon = rs.getString("icon");
                        String url = rs.getString("url");
                        Long parentId = rs.getLong("parent_id");
                        Long ordinal = rs.getLong("ordinal");
                        return new Menu(id, title, icon, url, parentId, ordinal);
                    });
            user.setRoles(role.stream().collect(Collectors.toSet()));
            user.setMenus(menus.stream().collect(Collectors.toSet()));

        }
        return Optional.ofNullable(user);
    }

    public Boolean existsByUserName(String username) {
        try {
            var user = jdbcTemplate.queryForObject(ScriptQuery.getUserIdByName, new Object[]{username}, Integer.class);
            return Objects.nonNull(user);
        } catch (EmptyResultDataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    public void save(User user, List<Role> roles) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(ScriptQuery.insertUser, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPassword());
            return ps;
        }, keyHolder);

        roles.forEach(x -> {
            jdbcTemplate.update(ScriptQuery.insertUserRole, keyHolder.getKey().intValue(), x.getId());
        });
    }

    public Optional<User> findById(Long id) {
        return Optional.empty();
    }
}
