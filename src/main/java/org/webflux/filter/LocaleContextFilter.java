package org.webflux.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.i18n.LocaleContextResolver;
import org.webflux.domain.BreadcrumbItem;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.parseLocale;

@Component
public class LocaleContextFilter implements WebFilter {

    private final LocaleContextResolver localeContextResolver;

    @Autowired
    public LocaleContextFilter(LocaleContextResolver localeContextResolver) {
        this.localeContextResolver = localeContextResolver;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .contextWrite(context -> {

                    ServerHttpRequest request = exchange.getRequest();
                    String path = request.getPath().toString();
                    if (!path.contains("/assets")) {
                        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
                        breadcrumbs.add(new BreadcrumbItem("Home", "/", false));
                        String[] paths = Arrays.stream(path.split("/")).filter(x -> !x.isBlank()).toArray(String[]::new);
                        if (paths.length > 1) {
                            breadcrumbs.add(new BreadcrumbItem(paths[0], "/", false));
                            breadcrumbs.add(new BreadcrumbItem(paths[1], path, true));
                        } else if (paths.length == 1) {
                            breadcrumbs.add(new BreadcrumbItem(paths[0], path, true));
                        }
                        // Add breadcrumbs to the model
                        exchange.getAttributes().put("breadcrumbs", breadcrumbs);
                    }

                    LocaleContext localeContext = localeContextResolver.resolveLocaleContext(exchange);
                    Locale locale = localeContext.getLocale();
                    exchange.getAttributes().put("locale", locale);
                    return context.put(Locale.class, locale);
                });
    }
}