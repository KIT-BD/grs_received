package com.grs.mobileApp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MobileOfficeOriginDTO {
    private Long id;
    private String office_name_bng;
    private String office_name_eng;
    private Long office_ministry_id;
    private Long office_layer_id;
    private Long parent_office_id;
    private Long office_level;
    private Long office_sequence;
    private MobileOfficeLayerDuplicateDTO office_layer;
}
