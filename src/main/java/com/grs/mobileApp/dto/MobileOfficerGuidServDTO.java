package com.grs.mobileApp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MobileOfficerGuidServDTO {
    private Long id;
    private Long office_id;
    private String office_name_bng;
    private Long office_unit_organogram_id;
    private Long office_unit_id;
    private Long employee_record_id;
    private String label;
    private String designation;
    private String unit_name_bng;
    private String name;
    private String name_en;
    private Boolean expanded;
    private Boolean checked;
}