package com.grs.api.mobileApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileGrievanceResponseDTO {

    private Long id;
    private String submission_date;
    private String submission_date_bn;
    private String complaint_type;
    private String complaint_type_bn;
    private String current_status;
    private String current_status_bn;
    private String subject;
    private String details;
    private Long grievance_from;
    private String tracking_number;
    private String tracking_number_bn;
    private Long complainant_id;
    private Long mygov_user_id;
    private Long triple_three_agent_id;
    private Boolean is_grs_user;
    private Long office_id;
    private Long service_id;
    private Long service_id_before_forward;
    private Long current_appeal_office_id;
    private Long current_appeal_office_unit_organogram_id;
    private Long send_to_ao_office_id;
    private Boolean is_anonymous;
    private Long case_number;
    private String other_service;
    private String other_service_before_forward;
    private String service_receiver;
    private String service_receiver_relation;
    private String gro_decision;
    private String gro_identified_complaint_cause;
    private String gro_suggestion;
    private String ao_decision;
    private String ao_identified_complaint_cause;
    private String ao_suggestion;
    private String created_at;
    private String updated_at;
    private String created_by;
    private String modified_by;
    private String status;
    private String rating;
    private String appeal_rating;
    private Boolean is_rating_given;
    private Boolean is_appeal_rating_given;
    private String feedback_comments;
    private String appeal_feedback_comments;
    private String source_of_grievance;
    private Boolean is_offline_complaint;
    private Boolean is_self_motivated_grievance;
    private String uploader_office_unit_organogram_id;
    private String possible_close_date;
    private String possible_close_date_bn;
    private Boolean is_evidence_provide;
    private Boolean is_see_hearing_date;
    private Boolean is_safety_net;
    private String complaint_category;
    private Long sp_programme_id;
    private Long geo_division_id;
    private Long geo_district_id;
    private Long geo_upazila_id;
}
