package com.grs.mobileApp.dto;

import lombok.Data;

@Data
public class MobileVisionDTO {
    private Long id;
    private Long office_origin_id;
    private String office_origin_name_bng;
    private String office_origin_name_eng;
    private Integer layer_level;
    private String vision_bng;
    private String vision_eng;
    private String mission_bng;
    private String mission_eng;
    private String expectations_bng;
    private String expectations_eng;
}
