package org.webflux.web.controller.rest;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.webflux.domain.DatabaseConfig;
import org.webflux.domain.UserDetailsImpl;
import org.webflux.enumerate.Status;
import org.webflux.model.StaticDestinationResponse;
import org.webflux.model.Toast;
import org.webflux.repository.DestinationRepository;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.util.Objects;
import java.util.Optional;

import static org.webflux.config.ClientFactory.getDataSource;

@Log4j2
@RestController
@RequestMapping("/api/database-config")
public class DestinationRestController {
    private DestinationRepository destinationRepository;

    @Autowired
    public void setCategoryRepository(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    @GetMapping
    public ResponseEntity<Toast> getAll(@Nullable @RequestParam("sortColumn") String sortColumn,
                                        @Nullable @RequestParam("sortType") String sortType,
                                        @RequestParam(name = "page", defaultValue = "1") int page,
                                        @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

        StaticDestinationResponse response;
        if (Objects.isNull(sortColumn) || Objects.isNull(sortType)) {
            response = destinationRepository.findAll(page, pageSize);
        } else {
            response = destinationRepository.findAllOrderBy(sortColumn, sortType, page, pageSize);
        }
        return ResponseEntity.ok(new Toast(Status.NoMessage, response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Toast> getOne(@PathVariable(name = "id") int configId) {
        Optional<DatabaseConfig> config = destinationRepository.findById(configId);
        if (config.isPresent()) {
            return ResponseEntity.ok(new Toast(Status.NoMessage, config));
        } else {
            return ResponseEntity.status(400).body(new Toast(Status.Error, null));
        }
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Toast>> updateOne(@RequestBody DatabaseConfig databaseConfig) {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(context -> {
                    Authentication authentication = context.getAuthentication();
                    if (Objects.nonNull(authentication)) {
                        if (authentication.getPrincipal() instanceof UserDetailsImpl principal) {
                            databaseConfig.setUpdatedBy(principal.getUser().getId());
                        }
                    } else {
                        databaseConfig.setUpdatedBy(0L); //anonymous
                    }

                    return databaseConfig;
                })
                .flatMap(x -> {
                    destinationRepository.updateOne(x);
                    return Mono.just(ResponseEntity.ok(new Toast(Status.Success, "Cập nhật", "Cập nhật danh mục thành công")));
                });
    }


    @PutMapping("/updateMany")
    public ResponseEntity<Toast> updateOne(@RequestParam(name = "configId") String configId, @RequestParam(name = "status") Boolean status) {
        destinationRepository.updateMany(configId, status);
        return ResponseEntity.ok(new Toast(Status.Success, "Cập nhật", "Cập nhật danh mục thành công"));
    }


    @PostMapping("/create")
    public Mono<ResponseEntity<Toast>> createEntity(@RequestBody DatabaseConfig databaseConfig) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    Authentication authentication = context.getAuthentication();
                    if (Objects.nonNull(authentication)) {
                        if (authentication.getPrincipal() instanceof UserDetailsImpl principal) {
                            databaseConfig.setCreatedBy(principal.getUser().getId());
                        }
                    } else {
                        databaseConfig.setCreatedBy(0L); //anonymous
                    }
                    return databaseConfig;
                })
                .flatMap(x -> {
                    destinationRepository.saveEntity(x);
                    return Mono.just(ResponseEntity.ok(new Toast(Status.Success, "Tạo mới", "Tạo mới danh mục thành công")));
                });
    }

    @GetMapping("/search")
    public ResponseEntity<Toast> search(@Nullable @RequestParam("name") String name,
                                        @Nullable @RequestParam("description") String description,
                                        @Nullable @RequestParam("url") String url) {

        StaticDestinationResponse response = destinationRepository.search(name, description, url);

        return ResponseEntity.ok(new Toast(Status.NoMessage, response));
    }

    @GetMapping("/connection/{id}")
    public Mono<ResponseEntity<Toast>> connection(@PathVariable(name = "id") int configId) {

        return ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    Authentication authentication = context.getAuthentication();
                    if (Objects.nonNull(authentication)) {
                        if (authentication.getPrincipal() instanceof UserDetailsImpl principal) {
                            if (checkConnection(configId)) {
                                destinationRepository.updateConnection(configId, principal.getUser().getId(), true);
                                return ResponseEntity.ok(new Toast(Status.Success, "Thông báo", "Kết nối thành công"));
                            } else {
                                destinationRepository.updateConnection(configId, principal.getUser().getId(), false);
                            }
                        }
                    }
                    return ResponseEntity.ok(new Toast(Status.Error, "Thông báo", "Kết nối không thành công"));
                });
    }

    private boolean checkConnection(int id) {
        Optional<DatabaseConfig> config = destinationRepository.findById(id);
        if (config.isPresent()) {
            try {
                var dbURL = "jdbc:oracle:thin:@//" + config.get().getUrl() + ":" + config.get().getPort() + "/" + config.get().getService();
                DataSource dataSource = getDataSource(config.get().getDriver(), dbURL, config.get().getUserName(), config.get().getPassword());
                dataSource.setLoginTimeout(5);
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                jdbcTemplate.setQueryTimeout(5);
                jdbcTemplate.execute("SELECT 1 FROM DUAL");
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return false;
    }
}