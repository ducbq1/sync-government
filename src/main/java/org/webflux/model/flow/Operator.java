package org.webflux.model.flow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operator {
    private Long top;
    private Long left;
    private Long typeOperator;
    private String nameOperator;
    private Long sourceOperator;
    private Properties properties;
}
