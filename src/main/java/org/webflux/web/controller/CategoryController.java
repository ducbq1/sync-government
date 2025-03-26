package org.webflux.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.webflux.domain.Category;
import org.webflux.enumerate.Status;
import org.webflux.helper.SessionExtension;
import org.webflux.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Controller
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {
    @Autowired
    private CategoryRepository categoryRepository;

    @RequestMapping("/resource")
    public String resource(WebSession session, ServerWebExchange serverWebExchange) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Danh mục tài nguyên");
        serverWebExchange.getAttributes().put("title", "Danh mục tài nguyên");
        serverWebExchange.getAttributes().put("type", "SOURCE,TABLE");
        return "landrick/dist/category/index";
    }

    @RequestMapping("/operator")
    public String operator(WebSession session, ServerWebExchange serverWebExchange) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Danh mục mẫu toán tử luồng");
        serverWebExchange.getAttributes().put("title", "Danh mục mẫu toán tử luồng");
        serverWebExchange.getAttributes().put("type", "VALIDATE,FUNCTION,SOURCE,DESTINATION");
        return "landrick/dist/category/index";
    }

    @RequestMapping("/external-api")
    public String externalApi(WebSession session, ServerWebExchange serverWebExchange) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Danh mục API mở rộng");
        serverWebExchange.getAttributes().put("title", "Danh mục API mở rộng");
        serverWebExchange.getAttributes().put("type", "DESTINATION");


        ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .filter(x -> x.isAuthenticated())
                .doOnNext(x -> System.out.println(x.getPrincipal()));

        return "landrick/dist/category/index";
    }

    @GetMapping("/get-category-by-type")
    public ResponseEntity<List<Category>> getCategoryByType(@RequestParam("type") String type) {
        List<String> lstType = new ArrayList<>();
        lstType.add(type);
        if("SOURCE".equals(type)) {
            lstType.add("SOURCE");
            lstType.add("TABLE");
        } else if("DESTINATION".equals(type)) {
            lstType.add("DESTINATION");
            lstType.add("TABLE");
        }
        List<Category> listResult = categoryRepository.findAllByType(lstType);
        return ResponseEntity.ok(listResult);
    }
}
