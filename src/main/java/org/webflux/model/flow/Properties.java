package org.webflux.model.flow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.webflux.model.flow.Operator;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Properties {
    private String title;
    private Map<String, Connector> inputs;
    private Map<String, Connector> outputs;
    private String body;
}
