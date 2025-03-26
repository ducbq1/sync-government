
package org.webflux.service.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.webflux.repository.SandboxDataRepository;
import org.webflux.service.SandboxDataService;
import reactor.core.publisher.Flux;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SandboxDataServiceImpl implements SandboxDataService {

    private final SandboxDataRepository repository;


    public Flux<Map<String, Object>> findAll() {
        return repository.findAll();
    }

    @Override
    public Flux<Map<String, Object>> executeSELECT(String query) {
        return repository.executeSELECT(query);
    }
}
