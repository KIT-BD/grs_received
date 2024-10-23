package com.grs.api.mobileApp.controller;

import com.grs.api.mobileApp.service.MobileGrievanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.grs.api.mobileApp.dto.MobileGrievanceResponseDTO;
import com.grs.api.mobileApp.dto.MobileResponse;
import com.grs.api.mobileApp.dto.MobileResponseNoList;
import com.grs.api.mobileApp.service.MobileGrievanceService;
import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.api.model.request.FileDTO;
import com.grs.api.model.request.GrievanceRequestDTO;
import com.grs.api.model.request.GrievanceWithoutLoginRequestDTO;
import com.grs.api.model.response.file.FileBaseDTO;
import com.grs.api.model.response.file.FileContainerDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.core.dao.GrievanceDAO;
import com.grs.core.domain.ServiceType;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.Grievance;
import com.grs.core.domain.grs.ServiceOrigin;
import com.grs.core.service.ComplainantService;
import com.grs.core.service.GrievanceService;
import com.grs.core.service.StorageService;
import com.grs.utils.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.WeakHashMap;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class MobileGrievanceController {

    @Autowired
    private MobileGrievanceService mobileGrievanceService;
    private final GrievanceService grievanceService;
    private final GrievanceDAO grievanceDAO;
    private final ComplainantService complainantService;
    private final StorageService storageService;

    @PostMapping(value = "/api/public-grievance/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WeakHashMap<String, Object>> savePublicGrievance(
            @RequestParam("officeId") String officeId,
            @RequestParam("description") String description,
            @RequestParam("subject") String subject,
            @RequestParam(value = "sp_programme_id", required = false) String spProgrammeId,
            @RequestParam("mobile_number") String mobileNumber,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "division_id", required = false) Integer divisionId,
            @RequestParam(value = "district_id", required = false) Integer districtId,
            @RequestParam(value = "upazila_id", required = false) Integer upazilaId,
            @RequestParam("complaint_category") Integer complaintCategory,
            @RequestParam(value = "fileNameByUser", required = false) String fileNameByUser,
            @RequestPart(value = "files[]", required = false) List<MultipartFile> files,
            Principal principal) throws Exception {

        // Call the service method
        WeakHashMap<String, Object> response = mobileGrievanceService.savePublicGrievanceService(
                officeId, description, subject, spProgrammeId, mobileNumber, name,
                email, divisionId, districtId, upazilaId, complaintCategory,
                fileNameByUser, files, principal
        );

        return ResponseEntity.ok(response);
    }

//    @RequestMapping(value = "/api/grievance", method = RequestMethod.GET)
//    public Page<GrievanceDTO> getGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
//        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
//        return grievanceService.findGrievancesByUsers(userInformation, pageable);
//    }

    @PostMapping("/api/grievance/save")
    public MobileResponseNoList submitMobileGrievanceWithLogin(
            Authentication authentication,
            @RequestParam("officeId") Long officeId,
            @RequestParam(value = "service_id", required = false) String serviceId,
            @RequestParam("description") String description,
            @RequestParam("subject") String subject,
            @RequestParam("complainant_id") Long complainantId,
            @RequestParam("is_grs_user") Boolean isGrsUser,
            @RequestParam(value = "fileNameByUser", required = false) String fileNameByUser,
            @RequestParam(value = "files[]", required = false) List<MultipartFile> files,
            Principal principal
    ) throws Exception {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long authComplainantId = userInformation.getUserId();
        if (!Objects.equals(authComplainantId, complainantId)){
            return MobileResponseNoList.builder()
                    .status("error")
                    .data("Invalid token for current user")
                    .build();
        }
        Complainant complainant = complainantService.findOne(complainantId);

        GrievanceWithoutLoginRequestDTO requestDTO = GrievanceWithoutLoginRequestDTO.builder()
                                                            .complainantPhoneNumber(complainant.getPhoneNumber())
                                                            .name(complainant.getName())
                                                            .email(complainant.getEmail())
                                                            .officeId(String.valueOf(officeId))
                                                            //.OfficeLayers("HQ>Division>District")
                                                            .serviceId(serviceId == null ? String.valueOf(ServiceType.NAGORIK.ordinal()) : serviceId)
                                                            .submissionDate(String.valueOf(new Date()))
                                                            .subject(subject)
                                                            .body(description)
                                                            .relation("Self")
                                                            .serviceReceiver(null)
                                                            .serviceOthers(null)
                                                            .isAnonymous(false)
                                                            .serviceType(ServiceType.NAGORIK)
                                                            .offlineGrievanceUpload(false)
                                                            .PhoneNumber(complainant.getPhoneNumber())
                                                            .isSelfMotivated(true)
                                                            .SourceOfGrievance(UserType.COMPLAINANT.name())
                                                            .user(complainant.getName())
                                                            .secret(null)
                                                            .submittedThroughApi(1)
                                                            .grievanceCategory(ServiceType.NAGORIK.ordinal())
                                                            .spProgrammeId(null)
                                                            .division(null)
                                                            .district(null)
                                                            .upazila(null)
                                                            .safetyNetId(1)
                                                            .divisionId(complainant.getPermanentAddressDivisionId())
                                                            .districtId(complainant.getPermanentAddressDistrictId())
                                                            .upazilaId(0)
                                                    .build();

        if (!files.isEmpty()) {
            FileContainerDTO fileContainerDTO = storageService.storeFileNew(principal, files.toArray(new MultipartFile[0]));
            List<FileBaseDTO> fileBaseDTOList = fileContainerDTO.getFiles();
            List<FileDTO> fileDTOS = new ArrayList<>();
            String[] fileNames = fileNameByUser.split(",");
            int i  = 0;
            for (FileBaseDTO f : fileBaseDTOList) {
                FileDerivedDTO fileDerivedDTO = (FileDerivedDTO) f;
                fileDTOS.add(
                        FileDTO.builder()
                                .name(fileNames[i++])
                                .url(fileDerivedDTO.getUrl())
                                .build()
                );
                System.out.println("Name: " + fileNames[i-1]);
                System.out.println("URL: " + fileDerivedDTO.getUrl());
            }
            requestDTO.setFiles(fileDTOS);
        }

        WeakHashMap<String, Object> addedGrievance = grievanceService.addGrievanceForOthers(authentication, requestDTO);

        String trackingNumber = addedGrievance.get("trackingNumber").toString();
        Grievance g = grievanceDAO.findByTrackingNumber(trackingNumber);

        MobileGrievanceResponseDTO responseDTO = MobileGrievanceResponseDTO.builder()
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
                .build();

        return MobileResponseNoList.builder()
                .status("success")
                .data(responseDTO)
                .build();

//        {
//            "status": "success",
//            "data": {
//                    "subject": "dfgsdfgsdfg",
//                    "submission_date": "2024-10-17 08:11:15am",
//                    "submission_date_bn": "২০২৪-১০-১৭ ০৮:১১:১৫পুর্বাহ্ন",
//                    "complaint_type": "Nagorik",
//                    "complaint_type_bn": "নাগরিক",
//                    "current_status": "New",
//                    "current_status_bn": "নতুন",
//                    "details": "<p>cxbvxcbxcvbcxvb</p>",
//                    "tracking_number": "01960782919101",
//                    "tracking_number_bn": "০১৯৬০৭৮২৯১৯১০১",
//                    "complainant_id": 29956,
//                    "is_grs_user": 1,
//                    "office_id": 28,
//                    "is_self_motivated_grievance": 1,
//                    "other_service": "অন্যান্য",
//                    "service_id": null,
//                    "source_of_grievance": "COMPLAINANT",
//                    "status": 1,
//                    "possible_close_date": "2024-09-07",
//                    "possible_close_date_bn": "২০২৪-০৯-০৭",
//                    "updated_at": "2024-10-17T08:11:15.000000Z",
//                    "created_at": "2024-10-17T08:11:15.000000Z",
//                    "id": 748
//             }
//        }
    }

    @GetMapping("/api/grievance/list")
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

    @GetMapping("/api/grievance/details")
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
