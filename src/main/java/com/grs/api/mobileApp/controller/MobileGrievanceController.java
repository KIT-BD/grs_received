package com.grs.api.mobileApp.controller;

import com.grs.api.mobileApp.dto.MobileGrievanceSubmissionResponseDTO;
import com.grs.api.mobileApp.dto.MobileResponse;
import com.grs.api.mobileApp.service.MobileGrievanceService;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.repo.projapoti.OfficeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import com.grs.api.mobileApp.dto.MobileGrievanceResponseDTO;
import com.grs.api.model.UserInformation;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.service.ComplainantService;
import com.grs.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class MobileGrievanceController {

    @Autowired
    private MobileGrievanceService mobileGrievanceService;
    private final ComplainantService complainantService;
    private final OfficeRepo officeRepo;

    @PostMapping(value = "/api/public-grievance/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MobileResponse savePublicGrievance(
            @RequestParam(value = "officeId", required = false) String officeId,
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
        MobileGrievanceSubmissionResponseDTO response = mobileGrievanceService.savePublicGrievanceService(
                officeId, description, subject, spProgrammeId, mobileNumber, name,
                email, divisionId, districtId, upazilaId, complaintCategory,
                fileNameByUser, files, principal
        );

        return MobileResponse.builder()
                .status("success")
                .data(response)
                .build();
    }

//    @RequestMapping(value = "/api/grievance", method = RequestMethod.GET)
//    public Page<GrievanceDTO> getGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
//        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
//        return grievanceService.findGrievancesByUsers(userInformation, pageable);
//    }

    @PostMapping("/api/grievance/save")
    public MobileResponse submitMobileGrievanceWithLogin(
            Authentication authentication,
            @RequestParam(value = "officeId", required = false) Long officeId,
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
            return MobileResponse.builder()
                    .status("error")
                    .data("Invalid token for current user")
                    .build();
        }
        Complainant complainant = complainantService.findOne(complainantId);

        MobileGrievanceSubmissionResponseDTO responseDTO = mobileGrievanceService.saveGrievanceWithLogin(
                authentication,
                complainant,
                officeId,
                serviceId,
                description,
                subject,
                isGrsUser,
                fileNameByUser,
                files,
                principal
        );

        return MobileResponse.builder()
                .status("success")
                .data(responseDTO)
                .build();
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

        List<MobileGrievanceResponseDTO> grievanceList = mobileGrievanceService.findGrievancesByUser(complainantId);

        return MobileResponse.builder()
                .status("success")
                .data(grievanceList)
                .build();
    }

    @GetMapping("/api/grievance/details")
    public Map<String, Object> getGrievanceDetails(
            @RequestParam("complaint_id") Long complaintId
    ) {
        MobileGrievanceResponseDTO grievanceList = mobileGrievanceService.findGrievancesById(complaintId);

        if (grievanceList == null){
            Map<String, Object> response = new HashMap<>();
            response.put("data", null);
            response.put("status", "success");

            return response;
        }

        Complainant complainant = complainantService.findOne(grievanceList.getComplainant_id());

        Map<String, Object> grievanceDetails = new HashMap<>();
        grievanceDetails.put("id", grievanceList.getId());
        grievanceDetails.put("submission_date", grievanceList.getSubmission_date());
        grievanceDetails.put("complaint_type", grievanceList.getComplaint_type());
        grievanceDetails.put("current_status", grievanceList.getCurrent_status());
        grievanceDetails.put("subject", grievanceList.getSubject());
        grievanceDetails.put("details", grievanceList.getDetails());
        grievanceDetails.put("tracking_number", grievanceList.getTracking_number());
        grievanceDetails.put("complainant_id", grievanceList.getComplainant_id());
        grievanceDetails.put("is_grs_user", grievanceList.getIs_grs_user());
        grievanceDetails.put("office_id", grievanceList.getOffice_id());
        grievanceDetails.put("service_id", grievanceList.getService_id());
        grievanceDetails.put("service_id_before_forward", grievanceList.getService_id_before_forward());
        grievanceDetails.put("current_appeal_office_id", grievanceList.getCurrent_appeal_office_id());
        grievanceDetails.put("current_appeal_office_unit_organogram_id", grievanceList.getCurrent_appeal_office_unit_organogram_id());
        grievanceDetails.put("send_to_ao_office_id", grievanceList.getSend_to_ao_office_id());
        grievanceDetails.put("is_anonymous", grievanceList.getIs_anonymous());
        grievanceDetails.put("case_number", grievanceList.getCase_number());
        grievanceDetails.put("other_service", grievanceList.getOther_service());
        grievanceDetails.put("other_service_before_forward", grievanceList.getOther_service_before_forward());
        grievanceDetails.put("service_receiver", grievanceList.getService_receiver());
        grievanceDetails.put("service_receiver_relation", grievanceList.getService_receiver_relation());
        grievanceDetails.put("gro_decision", grievanceList.getGro_decision());
        grievanceDetails.put("gro_identified_complaint_cause", grievanceList.getGro_identified_complaint_cause());
        grievanceDetails.put("gro_suggestion", grievanceList.getGro_suggestion());
        grievanceDetails.put("ao_decision", grievanceList.getAo_decision());
        grievanceDetails.put("ao_identified_complaint_cause", grievanceList.getAo_identified_complaint_cause());
        grievanceDetails.put("ao_suggestion", grievanceList.getAo_suggestion());
        grievanceDetails.put("created_at", grievanceList.getCreated_at());
        grievanceDetails.put("modified_at", grievanceList.getUpdated_at());
        grievanceDetails.put("created_by", grievanceList.getCreated_by());
        grievanceDetails.put("modified_by", grievanceList.getModified_by());
        grievanceDetails.put("status", grievanceList.getStatus());
        grievanceDetails.put("rating", grievanceList.getRating());
        grievanceDetails.put("appeal_rating", grievanceList.getAppeal_rating());
        grievanceDetails.put("is_rating_given", grievanceList.getIs_rating_given());
        grievanceDetails.put("is_appeal_rating_given", grievanceList.getIs_appeal_rating_given());
        grievanceDetails.put("feedback_comments", grievanceList.getFeedback_comments());
        grievanceDetails.put("appeal_feedback_comments", grievanceList.getAppeal_feedback_comments());
        grievanceDetails.put("source_of_grievance", grievanceList.getSource_of_grievance());
        grievanceDetails.put("is_offline_complaint", grievanceList.getIs_offline_complaint());
        grievanceDetails.put("is_self_motivated_grievance", grievanceList.getIs_self_motivated_grievance());
        grievanceDetails.put("uploader_office_unit_organogram_id", grievanceList.getUploader_office_unit_organogram_id());
        grievanceDetails.put("complaint_category", grievanceList.getComplaint_category());
        grievanceDetails.put("sp_programme_id", grievanceList.getSp_programme_id());
        grievanceDetails.put("geo_division_id", grievanceList.getGeo_division_id());
        grievanceDetails.put("geo_district_id", grievanceList.getGeo_district_id());
        grievanceDetails.put("geo_upazila_id", grievanceList.getGeo_upazila_id());
        grievanceDetails.put("is_safety_net", grievanceList.getIs_safety_net());
        grievanceDetails.put("medium_of_submission", null);
        grievanceDetails.put("complaint_attachment_info", null);

        SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        Map<String, Object> complainantInfo = new HashMap<>();
        complainantInfo.put("id", complainant.getId());
        complainantInfo.put("name", complainant.getName());
        complainantInfo.put("identification_value", complainant.getIdentificationValue());
        complainantInfo.put("identification_type", complainant.getIdentificationType().toString());  // Assuming `IdentificationType` is an enum
        complainantInfo.put("mobile_number", complainant.getPhoneNumber());
        complainantInfo.put("email", complainant.getEmail());
        complainantInfo.put("birth_date",  new SimpleDateFormat("yyyy-MM-dd").format(complainant.getBirthDate()));
        complainantInfo.put("occupation", complainant.getOccupation());
        complainantInfo.put("educational_qualification", complainant.getEducation());
        complainantInfo.put("gender", complainant.getGender() != null ? complainant.getGender().toString() : null);  // Assuming `Gender` is an enum
        complainantInfo.put("username", complainant.getUsername());
        complainantInfo.put("nationality_id", complainant.getCountryInfo() != null ? complainant.getCountryInfo().getId() : null);
        complainantInfo.put("present_address_street", complainant.getPresentAddressStreet());
        complainantInfo.put("present_address_house", complainant.getPresentAddressHouse());
        complainantInfo.put("present_address_division_id", complainant.getPresentAddressDivisionId());
        complainantInfo.put("present_address_division_name_bng", complainant.getPresentAddressDivisionNameBng());
        complainantInfo.put("present_address_division_name_eng", complainant.getPresentAddressDivisionNameEng());
        complainantInfo.put("present_address_district_id", complainant.getPresentAddressDistrictId());
        complainantInfo.put("present_address_district_name_bng", complainant.getPresentAddressDistrictNameBng());
        complainantInfo.put("present_address_district_name_eng", complainant.getPresentAddressDistrictNameEng());
        complainantInfo.put("present_address_type_id", complainant.getPresentAddressTypeId());
        complainantInfo.put("present_address_type_name_bng", complainant.getPresentAddressTypeNameBng());
        complainantInfo.put("present_address_type_name_eng", complainant.getPresentAddressTypeNameEng());
        complainantInfo.put("present_address_type_value", complainant.getPresentAddressTypeValue() != null ? complainant.getPresentAddressTypeValue().toString() : null);
        complainantInfo.put("present_address_postal_code", complainant.getPresentAddressPostalCode());
        complainantInfo.put("permanent_address_street", complainant.getPermanentAddressStreet());
        complainantInfo.put("permanent_address_house", complainant.getPermanentAddressHouse());
        complainantInfo.put("permanent_address_division_id", complainant.getPermanentAddressDivisionId());
        complainantInfo.put("permanent_address_division_name_bng", complainant.getPermanentAddressDivisionNameBng());
        complainantInfo.put("permanent_address_division_name_eng", complainant.getPermanentAddressDivisionNameEng());
        complainantInfo.put("permanent_address_district_id", complainant.getPermanentAddressDistrictId());
        complainantInfo.put("permanent_address_district_name_bng", complainant.getPermanentAddressDistrictNameBng());
        complainantInfo.put("permanent_address_district_name_eng", complainant.getPermanentAddressDistrictNameEng());
        complainantInfo.put("permanent_address_type_id", complainant.getPermanentAddressTypeId());
        complainantInfo.put("permanent_address_type_name_bng", complainant.getPermanentAddressTypeNameBng());
        complainantInfo.put("permanent_address_type_name_eng", complainant.getPermanentAddressTypeNameEng());
        complainantInfo.put("permanent_address_type_value", complainant.getPermanentAddressTypeValue() != null ? complainant.getPermanentAddressTypeValue().toString() : null);
        complainantInfo.put("permanent_address_postal_code", complainant.getPermanentAddressPostalCode());
        complainantInfo.put("foreign_permanent_address_line1", complainant.getForeignPermanentAddressLine1());
        complainantInfo.put("foreign_permanent_address_line2", complainant.getForeignPermanentAddressLine2());
        complainantInfo.put("foreign_permanent_address_city", complainant.getForeignPermanentAddressCity());
        complainantInfo.put("foreign_permanent_address_state", complainant.getForeignPermanentAddressState());
        complainantInfo.put("foreign_permanent_address_zipcode", complainant.getForeignPermanentAddressZipCode());
        complainantInfo.put("foreign_present_address_line1", complainant.getForeignPresentAddressLine1());
        complainantInfo.put("foreign_present_address_line2", complainant.getForeignPresentAddressLine2());
        complainantInfo.put("foreign_present_address_city", complainant.getForeignPresentAddressCity());
        complainantInfo.put("foreign_present_address_state", complainant.getForeignPresentAddressState());
        complainantInfo.put("foreign_present_address_zipcode", complainant.getForeignPresentAddressZipCode());
        complainantInfo.put("is_authenticated", complainant.isAuthenticated());
        complainantInfo.put("created_at", isoDateFormat.format(new Date(complainant.getCreatedAt().getTime())));
        complainantInfo.put("modified_at", isoDateFormat.format(new Date(complainant.getUpdatedAt().getTime())));
        complainantInfo.put("status", complainant.getStatus());
        complainantInfo.put("present_address_country_id", complainant.getPresentAddressCountryId());
        complainantInfo.put("permanent_address_country_id", complainant.getPermanentAddressCountryId());

        Office office = officeRepo.findOfficeById(grievanceList.getOffice_id());

        Map<String, Object> officeInfo = new HashMap<>();
        officeInfo.put("id", grievanceList.getOffice_id());
        officeInfo.put("nameBn", office.getNameBangla());
        officeInfo.put("name", office.getNameEnglish());
        officeInfo.put("code", "");
        officeInfo.put("division", office.getDivisionId());
        officeInfo.put("district", office.getDistrictId());
        officeInfo.put("upazila", office.getUpazilaId());
        officeInfo.put("phone", "");
        officeInfo.put("mobile", "");
        officeInfo.put("digitalNothiCode", "");
        officeInfo.put("fax", "");
        officeInfo.put("email", "");
        officeInfo.put("website", office.getWebsiteUrl());
        officeInfo.put("ministry", office.getOfficeMinistry().getId());
        officeInfo.put("layer", office.getOfficeLayer().getId());
        officeInfo.put("origin", office.getOfficeOriginId());
        officeInfo.put("customLayer", office.getOfficeLayer().getCustomLayerId());
        officeInfo.put("parentOfficeId", office.getParentOfficeId());


        Map<String, Object> data = new HashMap<>();
        data.put("complainant_info", complainantInfo);
        data.put("allComplaintDetails", grievanceDetails);
        data.put("doptoroffice", officeInfo);

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("status", "success");

        return response;
    }

    @GetMapping("/api/grievance-track")
    public MobileResponse getGrievanceByTrackingNumber(
            @RequestParam("tracking_number") String trx
    ){
        List<MobileGrievanceResponseDTO> grievanceList = mobileGrievanceService.findGrievancesByTrackingNumber(trx);

        if (grievanceList == null){
            return MobileResponse.builder()
                    .status("empty")
                    .data(new ArrayList<>())
                    .build();
        }
        if (grievanceList.size() > 1){
            return MobileResponse.builder()
                    .status("success")
                    .data(grievanceList.get(0))
                    .build();
        }
        return MobileResponse.builder()
                .status("success")
                .data(grievanceList)
                .build();
    }
}
