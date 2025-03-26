package org.webflux.domain.mission;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
public class CategoryEntity {

    private Long categoryId;
    private String name;
    private String description;
    private Boolean isActive;
    private Long deptId;
    private String deptName;
    private String code;
    private String categoryTypeCode;
    private Long value;
    private Long sortOrder;
    private String publishCode;
    private Long exploitationFee;
    private String note;
    private Long unit;
    private Long currentNumber;
}
