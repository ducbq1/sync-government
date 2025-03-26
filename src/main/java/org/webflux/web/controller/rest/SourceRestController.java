package org.webflux.web.controller.rest;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.webflux.domain.Category;
import org.webflux.domain.UserDetailsImpl;
import org.webflux.enumerate.Status;
import org.webflux.model.StaticSourceResponse;
import org.webflux.model.Toast;
import org.webflux.repository.SourceRepository;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("/api/source")
public class SourceRestController {
    private SourceRepository sourceRepository;

    @Autowired
    public void setCategoryRepository(SourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    @GetMapping
    public ResponseEntity<Toast> getAll(@Nullable @RequestParam("sortColumn") String sortColumn,
                                        @Nullable @RequestParam("sortType") String sortType,
                                        @RequestParam(name = "page", defaultValue = "1") int page,
                                        @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

        StaticSourceResponse response;
        if (Objects.isNull(sortColumn) || Objects.isNull(sortType)) {
            response = sourceRepository.findAll(page, pageSize);
        } else {
            response = sourceRepository.findAllOrderBy(sortColumn, sortType, page, pageSize);
        }
        return ResponseEntity.ok(new Toast(Status.NoMessage, response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Toast> getOne(@PathVariable(name = "id") int settingId) {
        Optional<Category> response = sourceRepository.findById(settingId);
        if (response.isPresent()) {
            return ResponseEntity.ok(new Toast(Status.NoMessage, response));
        } else {
            return ResponseEntity.status(400).body(new Toast(Status.Error, null));
        }
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Toast>> updateOne(@RequestBody Category category) {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(context -> {
                    Authentication authentication = context.getAuthentication();
                    if (Objects.nonNull(authentication)) {
                        if (authentication.getPrincipal() instanceof UserDetailsImpl principal) {
                            category.setUpdatedBy(principal.getUser().getId());
                        }
                    } else {
                        category.setUpdatedBy(0L); //anonymous
                    }

                    return category;
                })
                .flatMap(x -> {
                    sourceRepository.updateOne(x);
                    return Mono.just(ResponseEntity.ok(new Toast(Status.Success, "Cập nhật", "Cập nhật danh mục thành công")));
                });
    }


    @PutMapping("/updateMany")
    public ResponseEntity<Toast> updateOne(@RequestParam(name = "sourceId") String sourceId, @RequestParam(name = "status") Boolean status) {
        sourceRepository.updateMany(sourceId, status);
        return ResponseEntity.ok(new Toast(Status.Success, "Cập nhật", "Cập nhật danh mục thành công"));
    }


    @PostMapping("/create")
    public Mono<ResponseEntity<Toast>> createEntity(@RequestBody Category category) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    Authentication authentication = context.getAuthentication();
                    if (Objects.nonNull(authentication)) {
                        if (authentication.getPrincipal() instanceof UserDetailsImpl principal) {
                            category.setCreatedBy(principal.getUser().getId());
                        }
                    } else {
                        category.setCreatedBy(0L); //anonymous
                    }
                    return category;
                })
                .flatMap(x -> {
                    sourceRepository.saveEntity(x);
                    return Mono.just(ResponseEntity.ok(new Toast(Status.Success, "Tạo mới", "Tạo mới danh mục thành công")));
                });
    }

    @GetMapping("/search")
    public ResponseEntity<Toast> search(@Nullable @RequestParam("name") String name,
                                        @Nullable @RequestParam("description") String description,
                                        @Nullable @RequestParam("content") String content) {

        StaticSourceResponse response = sourceRepository.search(name, description, content);

        return ResponseEntity.ok(new Toast(Status.NoMessage, response));
    }
}