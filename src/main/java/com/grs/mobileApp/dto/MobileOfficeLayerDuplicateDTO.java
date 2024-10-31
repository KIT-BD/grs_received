package com.grs.mobileApp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MobileOfficeLayerDuplicateDTO {
    private Long id;
    private String layer_name_eng;
    private String layer_name_bng;
    private Integer layer_level;
}
