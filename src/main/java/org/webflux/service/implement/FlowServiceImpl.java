package org.webflux.service.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webflux.domain.SyncFlow;
import org.webflux.model.flow.Link;
import org.webflux.model.flow.MainFlow;
import org.webflux.model.flow.Operator;
import org.webflux.repository.FlowRepository;
import org.webflux.service.FlowService;
import org.webflux.service.dto.MapStructureDTO;
import org.webflux.service.dto.MapStructureDetailDTO;
import org.webflux.service.dto.SyncOperatorDTO;

import java.util.*;

@Service
public class FlowServiceImpl implements FlowService {
    @Autowired
    private FlowRepository flowRepository;
    @Override
    @Transactional
    public void save(String dataFlow) throws JsonProcessingException {
        //delete flow old
        flowRepository.deleteAllDataFlow();
        flowRepository.deleteAllMapStruct();
        flowRepository.deleteAllMapDetail();
        flowRepository.deleteAllSyncOperator();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(dataFlow, JsonNode.class);
        Long syncId = flowRepository.saveDataFlow(jsonNode.toString());

        MainFlow mainFlow = objectMapper.readValue(dataFlow, MainFlow.class);
        Map<String, Operator> lstOperator = mainFlow.getOperators();
        Map<String, Link> lstLink = mainFlow.getLinks();
        List<MapStructureDTO> lstMapStruct = new ArrayList<>();
        Map<String, Long> mapOperatorId = new HashMap<>();
        for(Map.Entry<String, Operator> operatorEntry : lstOperator.entrySet()) {
            if(!mapOperatorId.containsKey(operatorEntry.getKey())) {
                Long idOperator = flowRepository.saveSyncOperator(mapOperatorToSyncOperatorDTO(operatorEntry, syncId));
                mapOperatorId.put(operatorEntry.getKey(), idOperator);
            }
            for(Map.Entry<String, Link> linkEntry : lstLink.entrySet()) {
                if(linkEntry.getValue().getFromOperator().equals(operatorEntry.getKey())
                && !Boolean.TRUE.equals(linkEntry.getValue().getIsCheck())) {
                    MapStructureDTO item = new MapStructureDTO();
                    List<MapStructureDetailDTO> lstDetail = new ArrayList<>();
                    item.setSource(mapOperatorToSyncOperatorDTO(operatorEntry, syncId));
                    item.setSyncFlowId(syncId);
                    Optional<Map.Entry<String, Operator>> optionalToOperator = lstOperator.entrySet().stream()
                            .filter(x -> x.getKey().equals(linkEntry.getValue().getToOperator())).findFirst();
                    item.setDestination(mapOperatorToSyncOperatorDTO(optionalToOperator.get(), syncId));
                    for(Map.Entry<String, Link> linkEntry2 : lstLink.entrySet()) {
                        if(linkEntry2.getValue().getFromOperator().equals(operatorEntry.getKey())
                            && linkEntry2.getValue().getToOperator().equals(linkEntry.getValue().getToOperator())) {
                            linkEntry2.getValue().setIsCheck(true);
                            MapStructureDetailDTO itemDetail = new MapStructureDetailDTO();
                            itemDetail.setSourceField(operatorEntry.getValue().getProperties().getOutputs().get(linkEntry2.getValue().getFromConnector()).getLabel());
                            itemDetail.setDestinationField(optionalToOperator.get().getValue().getProperties().getInputs().get(linkEntry2.getValue().getToConnector()).getLabel());
                            //itemDetail.setFunction(operatorEntry.getValue().getProperties().getBody());
                            lstDetail.add(itemDetail);
                        }
                    }
                    item.setListDetail(lstDetail);
                    lstMapStruct.add(item);
                }
            }
        }
        saveMapStruct(lstMapStruct, mapOperatorId);
    }

    private String getBodyFunction(Operator functionOperator, String idOperator, Map<String, Link> lstLink, Map<String, Operator> lstOperator) {
        List<String> lstInput = functionOperator.getProperties().getInputs().keySet().stream().toList();
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, Link> lk : lstLink.entrySet()) {
            if(lk.getValue().getToOperator().equals(idOperator) && lstInput.contains(lk.getValue().getToConnector())) {
                String sourceFieldId = lk.getValue().getFromConnector();
                String sourceField = lstOperator.get(lk.getValue().getFromOperator()).getProperties().getOutputs().get(sourceFieldId).getLabel();
                map.put(functionOperator.getProperties().getInputs().get(lk.getValue().getToConnector()).getLabel(), sourceField);
            }
        }
        String result = functionOperator.getProperties().getBody();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return  result;
    }

    private void saveMapStruct(List<MapStructureDTO> mapStructureDTOS, Map<String, Long> mapOperatorId) {
        for (MapStructureDTO mapStructureDTO : mapStructureDTOS) {
            mapStructureDTO.getSource().setId(mapOperatorId.get(mapStructureDTO.getSource().getIdJson()));
            mapStructureDTO.getDestination().setId(mapOperatorId.get(mapStructureDTO.getDestination().getIdJson()));
            Long mapStructId = flowRepository.saveMapStruct(mapStructureDTO);
            for (MapStructureDetailDTO mapStructureDetailDTO : mapStructureDTO.getListDetail()) {
                mapStructureDetailDTO.setMapStructureId(mapStructId);
                flowRepository.saveMapDetail(mapStructureDetailDTO);
            }
        }
    }

    private SyncOperatorDTO mapOperatorToSyncOperatorDTO(Map.Entry<String, Operator> operatorEntry, Long syncId) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        SyncOperatorDTO result = new SyncOperatorDTO();
        result.setIdJson(operatorEntry.getKey());
        result.setName(operatorEntry.getValue().getProperties().getTitle());
        result.setOperatorData(objectMapper.writeValueAsString(operatorEntry.getValue()));
        result.setCategoryId(operatorEntry.getValue().getSourceOperator());
        result.setSyncFlowId(syncId);
        result.setBody(operatorEntry.getValue().getProperties().getBody());
        return result;
    }

    public List<MapStructureDTO> findAllMapStructure(Long syncFlowId) {
        return flowRepository.findAllMapStructure(syncFlowId);
    }

    public List<MapStructureDetailDTO> findAllMapStructureDetail(String mapStructureId) {
        return flowRepository.findAllMapStructureDetail(mapStructureId);
    }
    public List<SyncOperatorDTO> findAllSyncOperator(Long syncFlowId) {
        return flowRepository.findAllSyncOperator(syncFlowId);
    }

    public List<SyncFlow> findAll() {
        return flowRepository.findAll();
    }
}
