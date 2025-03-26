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
import org.webflux.service.dto.CategoryDTO;
import org.webflux.model.CategoryResponse;
import org.webflux.model.Toast;
import org.webflux.repository.CategoryRepository;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("/api/categories")
public class CategoryRestController {
    private CategoryRepository categoryRepository;

    @Autowired
    public void setCategoryRepository(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<Toast> getAll(@Nullable @RequestParam("sortColumn") String sortColumn,
                                        @Nullable @RequestParam("sortType") String sortType,
                                        @RequestParam(name = "page", defaultValue = "1") int page,
                                        @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
                                        @RequestParam("type") String type) {
        CategoryResponse response;

        // Fetch data
        if (Objects.nonNull(sortColumn) && Objects.nonNull(sortType)) {
            response = categoryRepository.findAllOrderBy(sortColumn, sortType, page, pageSize, type);
        } else {
            response = categoryRepository.findAll(page, pageSize, type);
        }

        return ResponseEntity.ok(new Toast(Status.NoMessage, response));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Toast> deleteMany( @RequestParam("ids") String ids) {
        categoryRepository.deleteMany(ids);
        return ResponseEntity.ok(new Toast(Status.Success, "Cập nhật", "Xóa danh mục thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Toast> getOne(@PathVariable(name = "id") int categoryId) {
        Optional<Category> response = categoryRepository.findById(categoryId);
        if (response.isPresent()) {
            return ResponseEntity.ok(new Toast(Status.NoMessage, response));
        } else {
            return ResponseEntity.status(400).body(new Toast(Status.Error, null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Toast> updateOne(@RequestBody CategoryDTO categoryDTO) {
        categoryRepository.update(categoryDTO);
        return ResponseEntity.ok(new Toast(Status.Success, "Cập nhật", "Cập nhật danh mục thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Toast> deleteOne(@PathVariable Long id) {
        categoryRepository.delete(id);
        return ResponseEntity.ok(new Toast(Status.Success, "Cập nhật", "Xóa danh mục thành công"));
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<Toast>> createEntity(@RequestBody CategoryDTO category) {
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
                    categoryRepository.saveCategory(x);
                    return Mono.just(ResponseEntity.ok(new Toast(Status.Success, "Tạo mới", "Tạo mới danh mục thành công")));
                });
    }

    @GetMapping("/search")
    public ResponseEntity<Toast> search(@Nullable @RequestParam("name") String name,
                                        @Nullable @RequestParam("type") String type,
                                        @Nullable @RequestParam("content") String content,
                                        @Nullable @RequestParam("description") String description,
                                        @Nullable @RequestParam("createDateFrom") String createDateFrom,
                                        @Nullable @RequestParam("createDateTo") String createDateTo) {

        CategoryResponse response = categoryRepository.search(type, name, content, description, createDateFrom, createDateTo);

        return ResponseEntity.ok(new Toast(Status.NoMessage, response));
    }
}
