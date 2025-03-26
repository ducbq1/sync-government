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
@RequiredArgsConstructor
public class LogController {

    @RequestMapping("/file-log")
    public String index(WebSession session, ServerWebExchange serverWebExchange) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Quản lý log file");
        serverWebExchange.getAttributes().put("title", "Quản lý log");
        return "landrick/dist/log/file-log";
    }

    @RequestMapping("/audit-log")
    public String flow(WebSession session, ServerWebExchange serverWebExchange) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Quản lý log action");
        serverWebExchange.getAttributes().put("title", "Quản lý log");
        return "landrick/dist/log/audit-log";
    }
}
