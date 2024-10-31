package com.grs.mobileApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileGrievanceSubmissionResponseDTO {
    private Long id;
    private String subject;
    private String submission_date;
    private String submission_date_bn;
    private String complaint_type;
    private String complaint_type_bn;
    private String current_status;
    private String current_status_bn;
    private String details;
    private String tracking_number;
    private String tracking_number_bn;
    private Long complainant_id;
    private Long is_grs_user;
    private Long office_id;
    private Long is_self_motivated_grievance;
    private String other_service;
    private Long service_id;
    private String source_of_grievance;
    private Long status;
    private String possible_close_date;
    private String possible_close_date_bn;
    private String updated_at;
    private String created_at;
}
