package org.webflux.repository;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.webflux.domain.PlaylistEntry;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public interface SandboxDataRepository {
    Flux<Map<String, Object>> findAll();
    Flux<Map<String, Object>> executeSELECT(String query);
}
