package org.webflux.service.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webflux.domain.SyncFlow;
import org.webflux.domain.SyncFlowStatic;
import org.webflux.model.StaticFlowResponse;
import org.webflux.model.flow.Link;
import org.webflux.model.flow.MainFlow;
import org.webflux.model.flow.Operator;
import org.webflux.repository.FlowRepository;
import org.webflux.repository.StaticFlowRepository;
import org.webflux.repository.query.SyncFlowStaticQuery;
import org.webflux.service.FlowService;
import org.webflux.service.StaticFlowService;
import org.webflux.service.dto.MapStructureDTO;
import org.webflux.service.dto.MapStructureDetailDTO;
import org.webflux.service.dto.SyncOperatorDTO;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class StaticFlowServiceImpl implements StaticFlowService {

    private StaticFlowRepository staticFlowRepository;
    private static final Pattern HOST_PORT_PATTERN = Pattern.compile("^(https?|ftp|file|static|dns)://([\\w.-]+):(\\d+)$");

    public List<SyncFlowStaticQuery> findAll() {
        return staticFlowRepository.findAll();
    }

    public StaticFlowResponse findAll(int page, int pageSize) {
        return staticFlowRepository.findAll(page, pageSize);
    }

    public StaticFlowResponse findAllOrderBy(String sortColumn, String sortType, int page, int pageSize) {
        return staticFlowRepository.findAllOrderBy(sortColumn, sortType, page, pageSize);
    }

    public Optional<SyncFlowStatic> findById(int id) {
        return staticFlowRepository.findById(id);
    }

    public StaticFlowResponse search(String name, String description, String sourceName, String destinationName) {
        return staticFlowRepository.search(name, description, sourceName, destinationName);
    }

    public Optional<Integer> updateOne(SyncFlowStatic syncFlowStatic) throws JsonProcessingException, NumberFormatException {
        validate(syncFlowStatic);
        return Optional.of(staticFlowRepository.updateOne(syncFlowStatic));
    }

    public void updateMany(String flowId, Boolean status) {
        staticFlowRepository.updateMany(flowId, status);
    }

    public void saveFlow(SyncFlowStatic syncFlowStatic) throws JsonProcessingException {
        validate(syncFlowStatic);
        staticFlowRepository.saveFlow(syncFlowStatic);
    }

    private void validate(SyncFlowStatic syncFlowStatic) throws JsonProcessingException, NumberFormatException {
        Matcher matcher = HOST_PORT_PATTERN.matcher(syncFlowStatic.getProxy());
        if (syncFlowStatic.getProxy().isBlank() || matcher.matches()) {
            if (matcher.matches()) {
                Integer.parseInt(matcher.group(3));
            }
        } else throw new NumberFormatException();

        ObjectMapper mapper = new ObjectMapper();
        syncFlowStatic.setPayload(mapper.readTree(syncFlowStatic.getPayload()).toPrettyString());
    }
}
