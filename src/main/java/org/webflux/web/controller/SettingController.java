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
@RequestMapping("/setting")
@RequiredArgsConstructor
public class SettingController {

    @RequestMapping("/source-api")
    public String source(WebSession session, ServerWebExchange serverWebExchange) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Cấu hình nguồn");
        serverWebExchange.getAttributes().put("title", "Cấu hình nguồn");
        return "landrick/dist/settings/source";
    }

    @RequestMapping("/destination")
    public String destination(WebSession session, ServerWebExchange serverWebExchange) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Cấu hình đích");
        serverWebExchange.getAttributes().put("title", "Cấu hình đích");
        return "landrick/dist/settings/destination";
    }

    @RequestMapping("/flow")
    public String flow(WebSession session, ServerWebExchange serverWebExchange) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Cấu hình luồng");
        serverWebExchange.getAttributes().put("title", "Cấu hình luồng");
        return "landrick/dist/settings/flow";
    }
}
