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
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    @RequestMapping("/role")
    public String role(WebSession session) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Quản lý nhóm quyền");
        return "landrick/dist/category/invoice-list";
    }

    @RequestMapping("/privilege")
    public String privilege(WebSession session) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Quản lý quyền");
        return "landrick/dist/category/invoice-list";
    }

    @RequestMapping("/list")
    public String list(WebSession session) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Danh sách người dùng");
        return "landrick/dist/category/invoice-list";
    }

    @RequestMapping("/menu")
    public String index(WebSession session) {
        SessionExtension.toastMessage(session, Status.Info, "Thông tin", "Danh sách điều hướng");
        return "landrick/dist/category/invoice-list";
    }
}
