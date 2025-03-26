package org.webflux.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.WebSession;
import org.webflux.enumerate.Status;
import org.webflux.helper.SessionExtension;

@Log4j2
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class StatisticController {

    @RequestMapping("/statistic")
    public String index(WebSession session) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Phân tích hệ thống");
        return "landrick/dist/category/invoice-list";
    }
}
