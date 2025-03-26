/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.webflux.web.controller.rest;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webflux.domain.PlaylistEntry;
import org.webflux.service.SandboxDataService;
import reactor.core.publisher.Flux;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api")
public class JsonRestController {

    private final SandboxDataService service;

    public JsonRestController(SandboxDataService service) {
        this.service = service;
    }

    @RequestMapping("/json")
    public String index() {
        return "Use '/smalllist.json' or '/biglist.json'";
    }


    @RequestMapping("/big.list.json")
    public Flux<Map<String, Object>> bigList() {
        return this.service.findAll();
    }

}
