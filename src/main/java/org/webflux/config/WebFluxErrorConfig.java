package org.webflux.config;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Configuration
public class WebFluxErrorConfig {

    @Bean
    @Order(-2)
    public ErrorWebExceptionHandler globalErrorWebExceptionHandler(CustomErrorAttributes errorAttributes) {
        return new GlobalErrorWebExceptionHandler(errorAttributes);
    }

    static class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

        private final CustomErrorAttributes errorAttributes;


        public GlobalErrorWebExceptionHandler(CustomErrorAttributes errorAttributes) {
            this.errorAttributes = errorAttributes;
        }

        @Override
        public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
            errorAttributes.storeErrorInformation(ex, exchange);
            return renderErrorResponse(ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders()))
                    .flatMap(response -> response.writeTo(exchange, new ServerResponse.Context() {
                        @Override
                        public List<HttpMessageWriter<?>> messageWriters() {
                            return HandlerStrategies.withDefaults().messageWriters();
                        }

                        @Override
                        public List<ViewResolver> viewResolvers() {
                            return HandlerStrategies.withDefaults().viewResolvers();
                        }
                    }));
        }

        private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
            Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(request, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.BINDING_ERRORS));
            return ServerResponse.status(HttpStatus.TEMPORARY_REDIRECT)
                    .location(URI.create("/error?error=" + URLEncoder.encode(errorAttributes.get("error").toString(), StandardCharsets.UTF_8)))
                    .body(BodyInserters.fromValue(errorAttributes));
        }
    }
}
