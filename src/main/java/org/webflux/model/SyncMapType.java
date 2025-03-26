package org.webflux.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncMapType {
    private String name;
    private Long functionId;
    private String functionType; // SQL, Javascript
    private String functionBody;
}