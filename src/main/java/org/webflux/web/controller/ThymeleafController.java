package org.webflux.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.WebSession;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import org.webflux.domain.PlaylistEntry;
import org.webflux.enumerate.Status;
import org.webflux.helper.SessionExtension;
import org.webflux.service.SandboxDataService;
import reactor.core.publisher.Flux;

import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
public class ThymeleafController {

    private final SandboxDataService service;

    @RequestMapping("/thymeleaf")
    public String index(WebSession session, final Model model) throws JsonProcessingException, ClassNotFoundException {
        return "landrick/dist/flow/index";
    }

    @RequestMapping("/thymeleaf2")
    public String index2(WebSession session, final Model model) throws JsonProcessingException, ClassNotFoundException {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Cẩn tắc vô ưu");
        return "landrick/dist/index";
    }


    @RequestMapping("/smalllist.thymeleaf")
    public String smallList(final Model model, WebSession session) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Cẩn tắc vô ưu");
        model.addAttribute("entries", this.service.findAll());
        return "thymeleaf/smalllist";
    }


    @RequestMapping("/biglist-datadriven.thymeleaf")
    public String bigListDataDriven(final Model model) {

        final Flux<Map<String, Object>> playlistStream = this.service.findAll();
        // No need to fully resolve the Publisher! We will just let it drive
        model.addAttribute("dataSource", new ReactiveDataDriverContextVariable(playlistStream, 1000));

        return "thymeleaf/biglist-datadriven";

    }


    @RequestMapping("/biglist-chunked.thymeleaf")
    public String bigListChunked(final Model model) {
        // Will be async resolved by Spring WebFlux before calling the view
        final Flux<Map<String, Object>> playlistStream = this.service.findAll();
        model.addAttribute("dataSource", playlistStream);
        return "thymeleaf/biglist-chunked";

    }


    @RequestMapping("/biglist-full.thymeleaf")
    public String bigListFull(final Model model) {

        // Will be async resolved by Spring WebFlux before calling the view
        final Flux<Map<String, Object>> playlistStream = this.service.findAll();

        model.addAttribute("dataSource", playlistStream);

        return "thymeleaf/biglist-full";

    }

}
