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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.Role;
import org.webflux.domain.User;
import org.webflux.enumerate.ScriptQuery;
import org.webflux.repository.RoleRepository;

import java.sql.Types;
import java.util.Optional;


@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private static final Logger log = LoggerFactory.getLogger(RoleRepositoryImpl.class);
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Role> findByName(String name) {
        try {
            var role = jdbcTemplate.queryForObject(ScriptQuery.getRoleByName, new Object[]{name}, new int[]{Types.VARCHAR},
                    (rs, rowNum) -> {
                        Long id = rs.getLong("id");
                        String nameRole = rs.getString("name");
                        String displayName = rs.getString("displayName");

                        return Role.builder().id(id).name(nameRole).displayName(displayName).build();
                    });
            return Optional.of(role);
        } catch (EmptyResultDataAccessException ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }

    }

    @Override
    public void save(Role role) {

    }
}
