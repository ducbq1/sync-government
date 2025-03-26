package org.webflux.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import org.thymeleaf.spring6.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import org.webflux.domain.Category;
import org.webflux.domain.SyncFlow;
import org.webflux.enumerate.Status;
import org.webflux.helper.SessionExtension;
import org.webflux.model.Toast;
import org.webflux.model.flow.MainFlow;
import org.webflux.repository.CategoryRepository;
import org.webflux.repository.FlowRepository;
import org.webflux.service.FlowService;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Log4j2
@Controller
@RequestMapping("/flow")
@RequiredArgsConstructor
public class FlowController {
    @Autowired
    private FlowService flowService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FlowRepository flowRepository;

    @RequestMapping("/view")
    public String view(Model model, WebSession session) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Xem và thiết lập luồng đồng bộ");
        Category source = Category.builder().id(1L).name("SOURCE").build();
        Category destination = Category.builder().id(2L).name("DESTINATION").build();
        Category function = Category.builder().id(3L).name("FUNCTION").build();
        Category validate = Category.builder().id(4L).name("VALIDATE").build();
        List<Category> lstType = List.of(source, destination, validate, function);
        List<Category> lstSource = categoryRepository.findAllByType(List.of("SOURCE", "DESTINATION", "FUNCTION", "VALIDATE"));
        List<SyncFlow> lstSyncFlow = flowRepository.findAll();
        model.addAttribute("lstType", lstType);
        model.addAttribute("lstSource", lstSource);
        model.addAttribute("syncFlow", CollectionUtils.isEmpty(lstSyncFlow) ? new SyncFlow() : lstSyncFlow.get(0));
        return "landrick/dist/flow/index";
    }

    @RequestMapping("/flow-out")
    public String flowOut(Model model, WebSession session) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Xem và thiết lập luồng đồng bộ");
        List<Category> lstType = categoryRepository.findAllByName(List.of("SOURCE", "DESTINATION", "FUNCTION", "VALIDATE"));
        List<Category> lstSource = categoryRepository.findAllByType(List.of("SOURCE", "DESTINATION", "FUNCTION", "VALIDATE"));
        List<SyncFlow> lstSyncFlow = flowRepository.findAll();
        model.addAttribute("lstType", lstType);
        model.addAttribute("lstSource", lstSource);
        model.addAttribute("syncFlow", CollectionUtils.isEmpty(lstSyncFlow) ? new SyncFlow() : lstSyncFlow.get(0));
        return "landrick/dist/flow/flow-out";
    }

    @RequestMapping("/error")
    public String error(Model model, WebSession session) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Thông tin luồng lỗi hoặc thất thoát");
        List<Category> lstType = categoryRepository.findAllByName(List.of("SOURCE", "DESTINATION", "FUNCTION", "VALIDATE"));
        List<Category> lstSource = categoryRepository.findAllByType(List.of("SOURCE", "DESTINATION", "FUNCTION", "VALIDATE"));
        List<SyncFlow> lstSyncFlow = flowRepository.findAll();
        model.addAttribute("lstType", lstType);
        model.addAttribute("lstSource", lstSource);
        model.addAttribute("syncFlow", CollectionUtils.isEmpty(lstSyncFlow) ? new SyncFlow() : lstSyncFlow.get(0));
        return "landrick/dist/flow/error";
    }

    @PostMapping ("/addNew")
    public ResponseEntity<Toast> saveFlow(@RequestBody String dataFlow) throws JsonProcessingException {
        flowService.save(dataFlow);
        return ResponseEntity.ok(new Toast(Status.Success, "Thông báo luồng", "Thêm thành công"));
    }

    @RequestMapping("/events")
    public String events(final Model model) {
        List<Integer> intList = Arrays.asList(1,2,5,7);
        var x = Flux.fromIterable(intList);

        final IReactiveDataDriverContextVariable dataDriver =
                new ReactiveDataDriverContextVariable(x, 1, 1);

        model.addAttribute("data", dataDriver);

        return "landrick/dist/dashboard/index";

    }

}
