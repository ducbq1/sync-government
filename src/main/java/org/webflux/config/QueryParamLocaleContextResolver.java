package org.webflux.config;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.i18n.LocaleContextResolver;

import java.util.Locale;
import java.util.Objects;

public class QueryParamLocaleContextResolver implements LocaleContextResolver {

    private static final String LANG_PARAM = "lang";

    @Override
    public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {
        String lang = exchange.getRequest().getQueryParams().getFirst(LANG_PARAM);
//        Locale locale = Objects.nonNull(lang) ? Locale.forLanguageTag(lang) : Locale.getDefault();
        Locale locale = Objects.nonNull(lang) ? Locale.forLanguageTag(lang) : Locale.forLanguageTag("vn");
        return new SimpleLocaleContext(locale);
    }

    @Override
    public void setLocaleContext(ServerWebExchange exchange, LocaleContext localeContext) {
        // This resolver does not support setLocaleContext
        throw new UnsupportedOperationException("Cannot change locale - use a different locale context resolution strategy");
    }
}
