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

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.webflux.domain.mapper.ResultSetMapper;
import org.webflux.repository.SandboxDataRepository;
import reactor.core.publisher.Flux;

import java.util.Map;

@Repository
public class SandboxDataRepositoryImpl implements SandboxDataRepository {

    private static final String QUERY_FIND_ALL_PLAYLIST_ENTRIES = "SELECT * FROM SANDBOX_DATA LIMIT 100";

    private JdbcTemplate jdbcTemplate;

    public SandboxDataRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Flux<Map<String, Object>> findAll() {

        return Flux.fromIterable(
                this.jdbcTemplate.query(
                        QUERY_FIND_ALL_PLAYLIST_ENTRIES, ResultSetMapper.mapRowToMap()
                )).repeat(1);

    }

    public Flux<Map<String, Object>> executeSELECT(String query) {
        return Flux.fromIterable(
                this.jdbcTemplate.query(query, ResultSetMapper.mapRowToMap()
                )).repeat(1);
    }

    public void executeOTHER(String query) {
        this.jdbcTemplate.execute(QUERY_FIND_ALL_PLAYLIST_ENTRIES);
    }
}
