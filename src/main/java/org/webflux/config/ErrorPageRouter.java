package org.webflux.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ErrorPageRouter {

    private final CustomErrorAttributes errorAttributes;

    public ErrorPageRouter(CustomErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @Bean
    public RouterFunction<ServerResponse> errorRoutes() {
        return route(GET("/error"), request -> {
            Optional<String> errorInfo = request.queryParam("error");
            if (errorInfo.isPresent()) {
                return ServerResponse.ok().render("landrick/dist/error", Map.of("error", errorInfo.get()));
            }
            return ServerResponse.permanentRedirect(URI.create("/")).build();
        });
    }
}
