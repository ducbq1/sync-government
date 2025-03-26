package org.webflux.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDinhKem {
    @JsonProperty("fileId")
    private String fileId;
    @JsonProperty("TypeId")
    private String TypeId;
    @JsonProperty("TenFile")
    private String TenFile;
}
