package com.grs.mobileApp.dto;

import lombok.Data;

@Data
public class MobileServiceListDTO {
    private Long id;
    private Long office_origin_id;
    private Long office_origin_unit_id;
    private Long office_origin_unit_organogram_id;
    private String office_origin_name_bng;
    private String office_origin_name_eng;
    private String service_type;
    private String service_name_bng;
    private String service_name_eng;
    private String service_procedure_bng;
    private String service_procedure_eng;
    private String documents_and_location_bng;
    private String documents_and_location_eng;
    private String payment_method_bng;
    private String payment_method_eng;
    private Integer service_time;
    private Integer status;
    private String created_by;
    private String modified_by;
    private String created_at;
    private String modified_at;
}
