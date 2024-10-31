package com.grs.mobileApp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MobileOfficeDTO {
    private Long id;
    private String office_name_bng;
    private String office_name_eng;
    private Integer geo_division_id;
    private Integer geo_district_id;
    private Integer geo_upazila_id;
    private String digital_nothi_code;
    private String office_phone;
    private String office_mobile;
    private String office_fax;
    private String office_email;
    private String office_web;
    private Long office_ministry_id;
    private Long office_layer_id;
    private Long office_origin_id;
    private Integer custom_layer_id;
    private Long parent_office_id;
    private MobileOfficeLayerDuplicateDTO office_layer;
}