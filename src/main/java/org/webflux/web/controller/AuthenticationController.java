package org.webflux.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.webflux.config.AuthenticationManager;
import org.webflux.config.PBKDF2Encoder;
import org.webflux.domain.Role;
import org.webflux.domain.User;
import org.webflux.enumerate.Status;
import org.webflux.helper.JWTUtil;
import org.webflux.helper.SessionExtension;
import org.webflux.model.AuthRequest;
import org.webflux.model.AuthResponse;
import org.webflux.service.RoleService;
import org.webflux.service.UserService;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Controller
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthenticationController {

    private final RoleService roleService;
    private JWTUtil jwtUtil;
    private PBKDF2Encoder passwordEncoder;
    private ReactiveUserDetailsService reactiveUserDetailsService;
    private UserService userService;
    private AuthenticationManager authenticationManager;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Mono<String> login(@ModelAttribute AuthRequest userInfo,
                        ServerWebExchange swe,
                        WebSession session,
                        Model model) {
        try {
            return reactiveUserDetailsService.findByUsername(userInfo.getUserName())
                    .filter(userDetails -> passwordEncoder.encode(userInfo.getPassword()).equals(userDetails.getPassword()))
                    .map(userDetails -> {
                        boolean rememberMe = Objects.nonNull(userInfo.getRememberMe()) && userInfo.getRememberMe();
                        return ResponseEntity.ok(new AuthResponse(jwtUtil.generateToken(userDetails, rememberMe)));
                    })
                    .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()))
                    .map(x -> {
                        if (x.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                            SessionExtension.toastMessage(session, Status.Error, "Thông tin", "Mật khẩu không chính xác");
                            return "redirect:/";                        } else {
                            ResponseCookie cookie = ResponseCookie.from("token", x.getBody().getToken()).path("/").maxAge(Integer.MAX_VALUE).build();
                            swe.getResponse().addCookie(cookie);
                            SessionExtension.toastMessage(session, Status.Success, "Thông tin", "Đăng nhập thành công");
                            return "redirect:/auth/login";
                        }
                    });

        } catch (UsernameNotFoundException usernameNotFoundException) {
            SessionExtension.toastMessage(session, Status.Error, "Thông tin", "Tên đăng nhập không tồn tại");
            return Mono.just("redirect:/auth/login");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            SessionExtension.toastMessage(session, Status.Error, "Thông tin", "Đăng nhập thất bại");
            return Mono.just("redirect:/auth/login");
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public Mono<String> login(Model model, WebSession session) {
        model.addAttribute("userInfo", new AuthRequest());
        return ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .filter(x -> x.isAuthenticated())
                .map(x -> "redirect:/")
                .switchIfEmpty(Mono.just("landrick/dist/auth/login"));

    }

    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public Mono<String> signup(Model model, WebSession session) {
        model.addAttribute("userInfo", new AuthRequest());
        return ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .filter(x -> x.isAuthenticated())
                .map(x -> "redirect:/")
                .switchIfEmpty(Mono.just("landrick/dist/auth/signup"));

    }


    @PostMapping("/signup")
    public String registerUser(@ModelAttribute AuthRequest userInfo,
                                          ServerWebExchange swe,
                                          WebSession session,
                                          Model model) {
        if (userService.existsByUserName(userInfo.getUserName())) {
            SessionExtension.toastMessage(session, Status.Error, "Thông tin", "Tên người dùng đã tồn tại");
            return "redirect:/auth/signup";
        }

        var user = User.builder()
                .userName(userInfo.getUserName())
                .password(passwordEncoder.encode(userInfo.getPassword()))
                .build();

        List<Role> roles = new ArrayList<>();
        Set<String> strRoles = Arrays.stream(userInfo.getRole().split(",")).map(String::trim).collect(Collectors.toSet());

        try {
            strRoles.forEach(role -> {
                roles.add(roleService.findByName(role.toUpperCase())
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found.")));
            });
        } catch (RuntimeException ex) {
            SessionExtension.toastMessage(session, Status.Error, "Thông tin", "Không tồn tại vai trò");
            return "redirect:/auth/signup";
        }

        userService.save(user, roles);

        var monoRes = reactiveUserDetailsService.findByUsername(userInfo.getUserName()).doOnSuccess(x -> {
            boolean rememberMe = Objects.nonNull(userInfo.getRememberMe()) && userInfo.getRememberMe();
            String jwt = jwtUtil.generateToken(x, rememberMe);
            ResponseCookie cookie = ResponseCookie.from("token", jwt).path("/").maxAge(Integer.MAX_VALUE).build();
            swe.getResponse().addCookie(cookie);
        }).subscribe();

        if (monoRes.isDisposed()) {
            return "redirect:/";
        } else {
            return "landrick/dist/auth/signup";
        }
    }

    @GetMapping("/logout")
    public String logoutUser(Model model, WebSession session, ServerWebExchange swe) {
        ResponseCookie cookie = ResponseCookie.from("token", null).path("/").httpOnly(true).maxAge(0).build();
        swe.getResponse().addCookie(cookie);
        model.addAttribute("userInfo", new AuthRequest());
        SessionExtension.toastMessage(session, Status.Success, "Thông tin", "Đăng xuất thành công");
        return "redirect:/auth/login";
    }
}