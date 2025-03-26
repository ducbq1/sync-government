package org.webflux.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.webflux.enumerate.Status;
import org.webflux.helper.SessionExtension;

@Log4j2
@Controller
@RequestMapping("/")
public class UtilityController {

    @RequestMapping("/utility")
    public String index(WebSession session) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Tiện ích");
        return "landrick/dist/category/sandbox";
    }

    @RequestMapping("/utility/sandbox")
    public String sandbox(WebSession session) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Tiện ích");
        return "landrick/dist/utility/sandbox";
    }

    @RequestMapping("/utility/management")
    public String mangement(WebSession session, ServerWebExchange exchange) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Tiện ích");
        exchange.getAttributes().put("title", "SQL Management");
        return "landrick/dist/utility/management";
    }
}
