package org.webflux.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

public interface SandboxDataService {
    Flux<Map<String, Object>> findAll();
    Flux<Map<String, Object>> executeSELECT(String query);
}