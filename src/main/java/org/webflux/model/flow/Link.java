package org.webflux.model.flow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Link {
    private String fromOperator;
    private String fromConnector;
    private Integer fromSubConnector;
    private String toOperator;
    private String toConnector;
    private Integer toSubConnector;
    private String color;
    private Boolean isCheck;
}
