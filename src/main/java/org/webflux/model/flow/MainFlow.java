package org.webflux.model.flow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.webflux.model.flow.Link;
import org.webflux.model.flow.Operator;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainFlow {
    @JsonProperty("operators")
    private Map<String, Operator> operators;

    @JsonProperty("links")
    private Map<String, Link> links;

    @JsonProperty("operatorTypes")
    private Map<String, Object> operatorTypes;
}
