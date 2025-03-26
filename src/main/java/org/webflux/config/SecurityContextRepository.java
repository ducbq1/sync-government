package org.webflux.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.webflux.domain.Menu;
import org.webflux.domain.UserDetailsImpl;
import org.webflux.helper.JWTUtil;
import org.webflux.service.UserService;
import org.webflux.service.implement.UserServiceImpl;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private JWTUtil jwtUtil;
    private AuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange swe, SecurityContext sc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange swe) {

        MultiValueMap<String, HttpCookie> cookieMap = swe.getRequest().getCookies();
        List<HttpCookie> cookies = cookieMap.get("token");
        if (Objects.nonNull(cookies)) {
            var authToken = cookies.get(0).getValue();
            if (Objects.nonNull(authToken)) {
                try {
                    Authentication auth = new UsernamePasswordAuthenticationToken(authToken, authToken);
                    return this.authenticationManager.authenticate(auth).doOnNext(x -> {
                        Claims claims = jwtUtil.getAllClaimsFromToken(authToken);

                        String path = swe.getRequest().getPath().value();
                        final List<String> urls = Arrays.stream(claims.get("urls").toString().split(",")).collect(Collectors.toList());
                        final List<String> acceptUrls = List.of("/", "/error", "/auth/logout");

                        if (!urls.contains(path) && !acceptUrls.contains(path)) {
                            //throw new AuthorizationServiceException("Not authorized");
                        }

                        if (x.getPrincipal() instanceof UserDetailsImpl principal) {
                            final Set<Menu> menus = principal.getUser().getMenus();
                            swe.getSession().subscribe(session -> session.getAttributes().put("menus", menus));
                            // hide menu at thymeleaf but not port gulp: 3000
                            swe.getSession().subscribe(session -> session.getAttributes().put("hideMenu", true));
                            ObjectMapper objectMapper = new ObjectMapper();
                            swe.getSession().subscribe(session -> {
                                try {
                                    session.getAttributes().put("menuJson", objectMapper.writeValueAsString(menus));
                                } catch (JsonProcessingException ex) {
                                    throw new RuntimeException(ex);
                                }
                            });
                        }

                    }).map(SecurityContextImpl::new);
                } catch (Exception ex) {
                    ResponseCookie cookie = ResponseCookie.from("token", null).path("/").httpOnly(true).maxAge(0).build();
                    swe.getResponse().addCookie(cookie);
                }
            }
        }

        return Mono.justOrEmpty(swe.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .flatMap(authHeader -> {
                    String authToken = authHeader.substring(7);
                    Authentication auth = new UsernamePasswordAuthenticationToken(authToken, authToken);
                    return this.authenticationManager.authenticate(auth).map(SecurityContextImpl::new);
                });
    }
}