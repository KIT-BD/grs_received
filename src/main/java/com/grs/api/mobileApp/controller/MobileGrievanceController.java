package com.grs.api.mobileApp.controller;

import com.grs.api.mobileApp.dto.MobileGrievanceResponseDTO;
import com.grs.api.mobileApp.dto.MobileResponse;
import com.grs.api.mobileApp.service.MobileGrievanceService;
import com.grs.api.model.UserInformation;
import com.grs.api.model.request.GrievanceRequestDTO;
import com.grs.core.dao.GrievanceDAO;
import com.grs.core.domain.grs.Grievance;
import com.grs.core.domain.grs.ServiceOrigin;
import com.grs.utils.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@Slf4j
@RequestMapping("/api/grievance")
@RequiredArgsConstructor
public class MobileGrievanceController {

    private final MobileGrievanceService mobileGrievanceService;

//    @RequestMapping(value = "/api/grievance", method = RequestMethod.GET)
//    public Page<GrievanceDTO> getGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
//        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
//        return grievanceService.findGrievancesByUsers(userInformation, pageable);
//    }

    @PostMapping("/save")
    public MobileResponse submitMobileGrievanceWithLogin(
            Authentication authentication,
            @RequestParam("officeId") Long officeId,
            @RequestParam(value = "service_id", required = false) String serviceId,
            @RequestParam("description") String description,
            @RequestParam("subject") String subject,
            @RequestParam("complainant_id") Long complainantId,
            @RequestParam("is_grs_user") Boolean isGrsUser,
            @RequestParam(value = "fileNameByUser", required = false) String fileNameByUser,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long authComplainantId = userInformation.getUserId();
        if (!Objects.equals(authComplainantId, complainantId)){
            return MobileResponse.builder()
                    .status("error")
                    .data(Collections.singletonList("Invalid token for current user"))
                    .build();
        }

        GrievanceRequestDTO requestDTO = GrievanceRequestDTO.builder()
                .serviceId(StringUtils.hasText(serviceId) ? serviceId : null)
                .body(description)
                .subject(subject)
                .officeId(String.valueOf(officeId))
                .build();

        Grievance addedGrievance = mobileGrievanceService.submitGrievance(userInformation, requestDTO);

        return MobileResponse.builder()
                .status("success")
                .data(Collections.singletonList(addedGrievance))
                .build();
    }

    @GetMapping("/list")
    public MobileResponse getGrievances(Authentication authentication){
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long complainantId = userInformation.getUserId();

        if (complainantId == null){
            return MobileResponse.builder()
                    .status("error")
                    .data(new ArrayList<>())
                    .build();
        }

        List<Grievance> grievanceList = mobileGrievanceService.findGrievancesByUser(complainantId);
        List<MobileGrievanceResponseDTO> grievanceDTOList = new ArrayList<>();

        for (Grievance g : grievanceList) {
            grievanceDTOList.add(
                    MobileGrievanceResponseDTO.builder()
                            .id(g.getId())
                            .submission_date(String.valueOf(g.getSubmissionDate()))
                            .submission_date_bn(String.valueOf(g.getSubmissionDate()))
                            .complaint_type(String.valueOf(g.getGrievanceType()))
                            .complaint_type_bn(String.valueOf(g.getGrievanceType()))
                            .current_status(String.valueOf(g.getGrievanceCurrentStatus()))
                            .current_status_bn(String.valueOf(g.getGrievanceCurrentStatus()))
                            .subject(g.getSubject())
                            .details(g.getDetails())
                            .grievance_from(g.getComplainantId())
                            .tracking_number(g.getTrackingNumber())
                            .tracking_number_bn(g.getTrackingNumber())
                            .complainant_id(g.getComplainantId())
                            .mygov_user_id(null)
                            .triple_three_agent_id(null)
                            .is_grs_user(g.isGrsUser())
                            .office_id(g.getOfficeId())
                            .service_id(Optional.ofNullable(g.getServiceOrigin()).map(ServiceOrigin::getId).orElse(null))
                            .service_id_before_forward(Optional.ofNullable(g.getServiceOriginBeforeForward()).map(ServiceOrigin::getId).orElse(null))
                            .current_appeal_office_id(g.getCurrentAppealOfficeId())
                            .current_appeal_office_unit_organogram_id(g.getCurrentAppealOfficerOfficeUnitOrganogramId())
                            .send_to_ao_office_id(g.getSendToAoOfficeId())
                            .is_anonymous(g.isAnonymous())
                            .case_number(Optional.ofNullable(g.getCaseNumber()).map(Long::valueOf).orElse(null))
                            .other_service(g.getOtherService())
                            .other_service_before_forward(g.getOtherServiceBeforeForward())
                            .service_receiver(g.getServiceReceiver())
                            .service_receiver_relation(g.getServiceReceiverRelation())
                            .gro_decision(g.getGroDecision())
                            .gro_identified_complaint_cause(g.getGroIdentifiedCause())
                            .gro_suggestion(g.getGroSuggestion())
                            .ao_decision(g.getAppealOfficerDecision())
                            .ao_identified_complaint_cause(g.getAppealOfficerIdentifiedCause())
                            .ao_suggestion(g.getAppealOfficerSuggestion())
                            .created_at(String.valueOf(g.getCreatedAt()))
                            .updated_at(String.valueOf(g.getUpdatedAt()))
                            .created_by(String.valueOf(g.getCreatedBy()))
                            .modified_by(String.valueOf(g.getModifiedBy()))
                            .status(String.valueOf(g.getStatus()))
                            .rating(Optional.ofNullable(g.getRating()).map(String::valueOf).orElse(null))
                            .appeal_rating(Optional.ofNullable(g.getAppealRating()).map(String::valueOf).orElse(null))
                            .is_rating_given(g.getIsRatingGiven())
                            .is_appeal_rating_given(g.getIsAppealRatingGiven())
                            .feedback_comments(g.getFeedbackComments())
                            .appeal_feedback_comments(g.getAppealFeedbackComments())
                            .source_of_grievance(g.getSourceOfGrievance())
                            .is_offline_complaint(g.getIsOfflineGrievance())
                            .is_self_motivated_grievance(g.getIsSelfMotivatedGrievance())
                            .uploader_office_unit_organogram_id(Optional.ofNullable(g.getUploaderOfficeUnitOrganogramId()).map(String::valueOf).orElse(null))
                            .possible_close_date(null)
                            .possible_close_date_bn(null)
                            .is_evidence_provide(null)
                            .is_see_hearing_date(null)
                            .is_safety_net(g.isSafetyNet())
                            .complaint_category(Optional.ofNullable(g.getComplaintCategory()).map(String::valueOf).orElse(null))
                            .sp_programme_id(Optional.ofNullable(g.getSpProgrammeId()).map(Long::valueOf).orElse(null))
                            .geo_division_id(Optional.ofNullable(g.getGeoDivisionId()).map(Long::valueOf).orElse(null))
                            .geo_district_id(Optional.ofNullable(g.getGeoDistrictId()).map(Long::valueOf).orElse(null))
                            .geo_upazila_id(Optional.ofNullable(g.getGeoUpazilaId()).map(Long::valueOf).orElse(null))
                            .build()
            );
        }

        return MobileResponse.builder()
                .status("success")
                .data(grievanceDTOList)
                .build();
    }

    @GetMapping("/details")
    public MobileResponse getGrievanceDetails(
            @RequestParam("complaint_id") Long complaintId
    ) {
        List<Grievance> grievanceList = mobileGrievanceService.findGrievancesByUser(complaintId);

        if (grievanceList == null){
            return MobileResponse.builder()
                    .status("error")
                    .data(new ArrayList<>())
                    .build();
        }

        List<MobileGrievanceResponseDTO> grievanceDTOList = new ArrayList<>();

        for (Grievance g : grievanceList) {
            grievanceDTOList.add(
                    MobileGrievanceResponseDTO.builder()
                            .id(g.getId())
                            .submission_date(String.valueOf(g.getSubmissionDate()))
                            .submission_date_bn(String.valueOf(g.getSubmissionDate()))
                            .complaint_type(String.valueOf(g.getGrievanceType()))
                            .complaint_type_bn(String.valueOf(g.getGrievanceType()))
                            .current_status(String.valueOf(g.getGrievanceCurrentStatus()))
                            .current_status_bn(String.valueOf(g.getGrievanceCurrentStatus()))
                            .subject(g.getSubject())
                            .details(g.getDetails())
                            .grievance_from(g.getComplainantId())
                            .tracking_number(g.getTrackingNumber())
                            .tracking_number_bn(g.getTrackingNumber())
                            .complainant_id(g.getComplainantId())
                            .mygov_user_id(null)
                            .triple_three_agent_id(null)
                            .is_grs_user(g.isGrsUser())
                            .office_id(g.getOfficeId())
                            .service_id(Optional.ofNullable(g.getServiceOrigin()).map(ServiceOrigin::getId).orElse(null))
                            .service_id_before_forward(Optional.ofNullable(g.getServiceOriginBeforeForward()).map(ServiceOrigin::getId).orElse(null))
                            .current_appeal_office_id(g.getCurrentAppealOfficeId())
                            .current_appeal_office_unit_organogram_id(g.getCurrentAppealOfficerOfficeUnitOrganogramId())
                            .send_to_ao_office_id(g.getSendToAoOfficeId())
                            .is_anonymous(g.isAnonymous())
                            .case_number(Optional.ofNullable(g.getCaseNumber()).map(Long::valueOf).orElse(null))
                            .other_service(g.getOtherService())
                            .other_service_before_forward(g.getOtherServiceBeforeForward())
                            .service_receiver(g.getServiceReceiver())
                            .service_receiver_relation(g.getServiceReceiverRelation())
                            .gro_decision(g.getGroDecision())
                            .gro_identified_complaint_cause(g.getGroIdentifiedCause())
                            .gro_suggestion(g.getGroSuggestion())
                            .ao_decision(g.getAppealOfficerDecision())
                            .ao_identified_complaint_cause(g.getAppealOfficerIdentifiedCause())
                            .ao_suggestion(g.getAppealOfficerSuggestion())
                            .created_at(String.valueOf(g.getCreatedAt()))
                            .updated_at(String.valueOf(g.getUpdatedAt()))
                            .created_by(String.valueOf(g.getCreatedBy()))
                            .modified_by(String.valueOf(g.getModifiedBy()))
                            .status(String.valueOf(g.getStatus()))
                            .rating(Optional.ofNullable(g.getRating()).map(String::valueOf).orElse(null))
                            .appeal_rating(Optional.ofNullable(g.getAppealRating()).map(String::valueOf).orElse(null))
                            .is_rating_given(g.getIsRatingGiven())
                            .is_appeal_rating_given(g.getIsAppealRatingGiven())
                            .feedback_comments(g.getFeedbackComments())
                            .appeal_feedback_comments(g.getAppealFeedbackComments())
                            .source_of_grievance(g.getSourceOfGrievance())
                            .is_offline_complaint(g.getIsOfflineGrievance())
                            .is_self_motivated_grievance(g.getIsSelfMotivatedGrievance())
                            .uploader_office_unit_organogram_id(Optional.ofNullable(g.getUploaderOfficeUnitOrganogramId()).map(String::valueOf).orElse(null))
                            .possible_close_date(null)
                            .possible_close_date_bn(null)
                            .is_evidence_provide(null)
                            .is_see_hearing_date(null)
                            .is_safety_net(g.isSafetyNet())
                            .complaint_category(Optional.ofNullable(g.getComplaintCategory()).map(String::valueOf).orElse(null))
                            .sp_programme_id(Optional.ofNullable(g.getSpProgrammeId()).map(Long::valueOf).orElse(null))
                            .geo_division_id(Optional.ofNullable(g.getGeoDivisionId()).map(Long::valueOf).orElse(null))
                            .geo_district_id(Optional.ofNullable(g.getGeoDistrictId()).map(Long::valueOf).orElse(null))
                            .geo_upazila_id(Optional.ofNullable(g.getGeoUpazilaId()).map(Long::valueOf).orElse(null))
                            .build()
            );
        }

        return MobileResponse.builder()
                .status("success")
                .data(grievanceDTOList)
                .build();
    }
}
