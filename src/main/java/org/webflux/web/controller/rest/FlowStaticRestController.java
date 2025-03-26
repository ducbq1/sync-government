package org.webflux.web.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.webflux.domain.SyncFlowStatic;
import org.webflux.domain.UserDetailsImpl;
import org.webflux.enumerate.Status;
import org.webflux.model.StaticFlowResponse;
import org.webflux.model.Toast;
import org.webflux.repository.StaticFlowRepository;
import org.webflux.service.StaticFlowService;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("/api/flow")
public class FlowStaticRestController {
    private StaticFlowService staticFlowService;

    @Autowired
    public void setCategoryRepository(StaticFlowService staticFlowService) {
        this.staticFlowService = staticFlowService;
    }

    @GetMapping
    public ResponseEntity<Toast> getAll(@Nullable @RequestParam("sortColumn") String sortColumn,
                                        @Nullable @RequestParam("sortType") String sortType,
                                        @RequestParam(name = "page", defaultValue = "1") int page,
                                        @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

        if (Objects.isNull(sortColumn) || Objects.isNull(sortType)) {
            StaticFlowResponse response = staticFlowService.findAll(page, pageSize);
            return ResponseEntity.ok(new Toast(Status.NoMessage, response));
        } else {
            StaticFlowResponse response = staticFlowService.findAllOrderBy(sortColumn, sortType, page, pageSize);
            return ResponseEntity.ok(new Toast(Status.NoMessage, response));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Toast> getOne(@PathVariable(name = "id") int settingId) {
        Optional<SyncFlowStatic> response = staticFlowService.findById(settingId);
        if (response.isPresent()) {
            return ResponseEntity.ok(new Toast(Status.NoMessage, response));
        } else {
            return ResponseEntity.status(400).body(new Toast(Status.Error, null));
        }
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Toast>> updateOne(@RequestBody SyncFlowStatic syncFlowStatic) {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(context -> {
                    Authentication authentication = context.getAuthentication();
                    if (Objects.nonNull(authentication)) {
                        if (authentication.getPrincipal() instanceof UserDetailsImpl principal) {
                            syncFlowStatic.setUpdatedBy(principal.getUser().getId());
                        }
                    } else {
                        syncFlowStatic.setUpdatedBy(0L); //anonymous
                    }

                    return syncFlowStatic;
                })
                .flatMap(x -> {
                    try {
                        staticFlowService.updateOne(x);
                    } catch (JsonProcessingException ex) {
                        return Mono.just(ResponseEntity.ok(new Toast(Status.Warning, "Tạo mới", "Payload không đúng định dạng JSON")));
                    } catch (NumberFormatException ex) {
                        return Mono.just(ResponseEntity.ok(new Toast(Status.Warning, "Tạo mới", "Không đúng định dạng proxy")));
                    }
                    return Mono.just(ResponseEntity.ok(new Toast(Status.Success, "Cập nhật", "Cập nhật danh mục thành công")));
                });
    }

    @PutMapping("/updateMany")
    public ResponseEntity<Toast> updateMany(@RequestParam(name = "flowID") String flowId, @RequestParam(name = "status") Boolean status) {
        staticFlowService.updateMany(flowId, status);
        return ResponseEntity.ok(new Toast(Status.Success, "Cập nhật", "Cập nhật danh mục thành công"));
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<Toast>> createEntity(@RequestBody SyncFlowStatic syncFlowStatic) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    Authentication authentication = context.getAuthentication();
                    if (Objects.nonNull(authentication)) {
                        if (authentication.getPrincipal() instanceof UserDetailsImpl principal) {
                            syncFlowStatic.setCreatedBy(principal.getUser().getId());
                        }
                    } else {
                        syncFlowStatic.setCreatedBy(0L); //anonymous
                    }
                    return syncFlowStatic;
                })
                .flatMap(x -> {
                    try {
                        staticFlowService.saveFlow(x);
                    } catch (JsonProcessingException ex) {
                        return Mono.just(ResponseEntity.ok(new Toast(Status.Warning, "Tạo mới", "Payload không đúng định dạng JSON")));
                    } catch (NumberFormatException ex) {
                        return Mono.just(ResponseEntity.ok(new Toast(Status.Warning, "Tạo mới", "Không đúng định dạng proxy")));
                    }
                    return Mono.just(ResponseEntity.ok(new Toast(Status.Success, "Tạo mới", "Tạo mới danh mục thành công")));
                });
    }

    @GetMapping("/search")
    public ResponseEntity<Toast> search(@Nullable @RequestParam("name") String name,
                                        @Nullable @RequestParam("description") String description,
                                        @Nullable @RequestParam("sourceName") String sourceName,
                                        @Nullable @RequestParam("destinationName") String destinationName) {

        StaticFlowResponse response = staticFlowService.search(name, description, sourceName, destinationName);

        return ResponseEntity.ok(new Toast(Status.NoMessage, response));
    }
}