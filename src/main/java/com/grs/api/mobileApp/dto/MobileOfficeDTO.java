package com.grs.api.mobileApp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MobileOfficeDTO {
    private int id;
    private String office_name_bng;
    private String office_name_eng;
    private int geo_division_id;
    private int geo_district_id;
    private int geo_upazila_id;
    private String digital_nothi_code;
    private String office_phone;
    private String office_mobile;
    private String office_fax;
    private String office_email;
    private String office_web;
    private int office_ministry_id;
    private int office_layer_id;
    private int office_origin_id;
    private int custom_layer_id;
    private int parent_office_id;
    private MobileOfficeLayerDuplicateDTO office_layer;
}