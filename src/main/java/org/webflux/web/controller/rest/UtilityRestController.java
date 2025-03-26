/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
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
package org.webflux.web.controller.rest;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webflux.domain.DatabaseConfig;
import org.webflux.domain.mapper.ResultSetMapper;
import org.webflux.enumerate.Status;
import org.webflux.model.Toast;
import org.webflux.repository.DatabaseConfigRepository;
import org.webflux.repository.DestinationRepository;
import org.webflux.repository.query.DatabaseConfigQuery;
import org.webflux.service.DatabaseConfigService;
import org.webflux.service.SandboxDataService;
import org.webflux.service.dto.QueryDTO;
import reactor.core.publisher.Flux;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.webflux.config.ClientFactory.getDataSource;

@Log4j2
@RestController
@RequestMapping("/api")
public class UtilityRestController {
    private final SandboxDataService service;
    private final DatabaseConfigRepository databaseConfigRepository;
    private final DestinationRepository destinationRepository;

    public UtilityRestController(SandboxDataService service,  DatabaseConfigRepository databaseConfigRepository, DestinationRepository destinationRepository) {
        this.service = service;
        this.databaseConfigRepository = databaseConfigRepository;
        this.destinationRepository = destinationRepository;
    }

    @RequestMapping("/utility/json")
    public String index() {
        return "Use '/smalllist.json' or '/biglist.json'";
    }


    @RequestMapping("/utility/sandbox")
    public Flux<Map<String, Object>> bigList() {
        return this.service.findAll();
    }

    @RequestMapping("/utility")
    public ResponseEntity<Toast> init() {
        var databaseConfigs = databaseConfigRepository.findAll();
        return ResponseEntity.ok(new Toast(Status.NoMessage, databaseConfigs));
    }

    @RequestMapping("/utility/sql")
    public ResponseEntity<Toast> sql(@RequestBody QueryDTO queryDTO) {
        Optional<DatabaseConfigQuery> config = databaseConfigRepository.findById(queryDTO.getDatabaseConfigId());
        List<List<Map<String, Object>>> lstData = new ArrayList<>();
        if (config.isPresent()) {
            try {
                var dbURL = config.get().getUrl();
                DataSource dataSource = getDataSource(config.get().getDriver(), dbURL, config.get().getUserName(), config.get().getPassword());
                dataSource.setLoginTimeout(5);
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                jdbcTemplate.setQueryTimeout(5);
                if (Strings.isNotBlank(queryDTO.getQuery())) {
                    String[] queries = queryDTO.getQuery().split(";");
                    for (String query : queries) {
                        if (query.trim().toLowerCase().startsWith("select")) {
                            var data = jdbcTemplate.query(query, ResultSetMapper.mapRowToMap());
                            lstData.add(data);
                        } else {
                            jdbcTemplate.execute(query);
                        }
                    }
                    if (CollectionUtils.isEmpty(lstData)) {
                        return ResponseEntity.ok(new Toast(Status.Success, "Thông báo", "Cập nhật thành công"));
                    } else {
                        return ResponseEntity.ok(new Toast(Status.NoMessage, lstData));
                    }
                }
            } catch (Exception ex) {
                return ResponseEntity.ok(new Toast(Status.Error, "Thông báo", ex.getMessage()));
            }
        }

        return ResponseEntity.ok(new Toast(Status.Error, "Thông báo", "Không tìm thấy database"));
    }
}
