package com.grs.mobileApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileSpProgrammeDTO {
    private Integer id;
    private String name_en;
    private String name_bn;
    private Long office_id;
    private Integer status;
}
