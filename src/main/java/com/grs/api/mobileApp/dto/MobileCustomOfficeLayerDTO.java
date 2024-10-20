package com.grs.api.mobileApp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MobileCustomOfficeLayerDTO {
    private Long id;
    private String name;
    private Integer layer_level;
    private String name_en;
}
