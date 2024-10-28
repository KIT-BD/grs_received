package com.grs.mobileApp.dto;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.core.domain.RoleType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MobileGrievanceForwardingDTO {
    private Long id;
    private Integer complaint_id;
    private String note;
    private String action;
    private Integer to_employee_record_id;
    private Integer from_employee_record_id;
    private Integer to_office_unit_organogram_id;
    private Integer from_office_unit_organogram_id;
    private Integer to_office_id;
    private Integer from_office_id;
    private Integer to_office_unit_id;
    private Integer from_office_unit_id;
    private Integer is_current;
    private Integer is_cc;
    private Integer is_committee_head;
    private Integer is_committee_member;
    private String to_employee_name_bng;
    private String from_employee_name_bng;
    private String to_employee_name_eng;
    private String from_employee_name_eng;
    private String to_employee_designation_bng;
    private String from_employee_designation_bng;
    private String to_office_name_bng;
    private String from_office_name_bng;
    private String to_employee_unit_name_bng;
    private String from_employee_unit_name_bng;
    private String from_employee_username;
    private String from_employee_signature;
    private String created_at;
    private String updated_at;
    private String created_by;
    private String modified_by;
    private String status;
    private String deadline_date;
    private String current_status;
    private Integer is_seen;
    private RoleType assigned_role;
    private List<FileDerivedDTO> complain_movement_attachment;
}
