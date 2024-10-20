package com.grs.api.mobileApp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MobileOfficeLayerDuplicateDTO {
    private int id;
    private String layer_name_eng;
    private String layer_name_bng;
    private int layer_level;
}
