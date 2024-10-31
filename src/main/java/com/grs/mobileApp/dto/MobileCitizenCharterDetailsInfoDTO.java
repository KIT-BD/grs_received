package com.grs.mobileApp.dto;

import lombok.Data;

@Data
public class MobileCitizenCharterDetailsInfoDTO {
    private Long id;
    private Long office_id;
    private Long office_origin_id;
    private Long service_id;
    private Long so_office_id;
    private Long so_office_unit_id;
    private Long so_office_unit_organogram_id;
    private String service_name_bng;
    private String service_name_eng;
    private String service_procedure_bng;
    private String service_procedure_eng;
    private String documents_and_location_bng;
    private String documents_and_location_eng;
    private String payment_method_bng;
    private String payment_method_eng;
    private Integer service_time;
    private String service_type;
    private Boolean is_disabled_for_admin;
    private Integer status;
    private Integer origin_status;
    private String created_at;
    private String modified_at;
    private String created_by;
    private String modified_by;
}
