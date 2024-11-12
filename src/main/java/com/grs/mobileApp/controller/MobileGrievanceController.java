package com.grs.mobileApp.controller;

import com.grs.api.model.request.GrievanceForwardingNoteDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.core.model.ListViewType;
import com.grs.core.service.GrievanceService;
import com.grs.mobileApp.dto.*;
import com.grs.mobileApp.service.MobileGrievanceService;
import com.grs.mobileApp.service.MobilePublicAPIService;
import com.grs.api.model.response.GrievanceForwardingEmployeeRecordsDTO;
import com.grs.core.domain.grs.Education;
import com.grs.core.domain.grs.Occupation;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.repo.projapoti.OfficeRepo;
import com.grs.core.service.GrievanceForwardingService;
import com.grs.utils.BanglaConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import com.grs.api.model.UserInformation;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.service.ComplainantService;
import com.grs.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.text.ParseException;
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
    private final GrievanceForwardingService grievanceForwardingService;
    private final MobilePublicAPIService mobilePublicAPIService;
    private final GrievanceService grievanceService;

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
    public MobileResponse getGrievances(
            Authentication authentication,
            @RequestParam("complainant_id") Long id
    ) throws ParseException {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long complainantId = userInformation.getUserId();

        if (!Objects.equals(complainantId, id)){
            return MobileResponse.builder()
                    .status("error")
                    .data("Invalid token for complainant id")
                    .build();
        }

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

    @PostMapping(value = "/api/administrative-grievance/request-appeal")
    public Map<String, Object> sendForAppeal(
            @RequestParam("complaint_id") Long complaint_id,
            @RequestParam("note") String note,
            Authentication authentication) {
        GrievanceForwardingNoteDTO grievanceForwardingNoteDTO = GrievanceForwardingNoteDTO.builder()
                .grievanceId(complaint_id)
                .note(note)
                .build();
        GenericResponse genericResponse = grievanceForwardingService.appealToOfficer(grievanceForwardingNoteDTO, authentication);

        Map<String, Object> response = new HashMap<>();
        response.put("status", genericResponse.isSuccess() ? "success" : "error");
        response.put("message", genericResponse.getMessage());
        return response;
    }

    @GetMapping("/api/grievance/complainant/movement")
    public Map<String,Object> getMovementForComplainant(
            Authentication authentication,
            @RequestParam("complaint_id") Long id
    ) throws ParseException {
        if (id == null){
            Map<String, Object> response = new HashMap<>();
            response.put("data", "Complaint could not be found");
            response.put("status", "error");

            return response;
        }
        List<GrievanceForwardingEmployeeRecordsDTO> grievanceList = grievanceForwardingService.getAllComplaintMovementHistoryByGrievance(id, authentication);
        List<MobileGrievanceForwardingDTO> forwardingDTOList = new ArrayList<>();

        for (GrievanceForwardingEmployeeRecordsDTO g : grievanceList){
            forwardingDTOList.add(
                    MobileGrievanceForwardingDTO.builder()
                            .id(null)
                            .complaint_id(Math.toIntExact(id))
                            .note(g.getComment())
                            .action(g.getAction())
                            .to_employee_record_id(null)
                            .from_employee_record_id(null)
                            .to_office_unit_organogram_id(null)
                            .from_office_unit_organogram_id(null)
                            .to_office_id(null)
                            .from_office_id(null)
                            .to_office_unit_id(null)
                            .from_office_unit_id(null)
                            .is_current(null)
                            .is_cc(g.getIsCC() ? 1 : 0)
                            .is_committee_head(g.getIsCommitteeHead() ? 1 : 0)
                            .is_committee_member(g.getIsCommitteeMember() ? 1 : 0)
                            .to_employee_name_bng(g.getToGroNameBangla())
                            .from_employee_name_bng(g.getFromGroNameBangla())
                            .to_employee_name_eng(g.getToGroNameEnglish())
                            .from_employee_name_eng(g.getFromGroNameEnglish())
                            .to_employee_designation_bng(g.getToDesignationNameBangla())
                            .from_employee_designation_bng(g.getFromDesignationNameBangla())
                            .to_office_name_bng(g.getToOfficeNameBangla())
                            .from_office_name_bng(g.getFromOfficeNameBangla())
                            .to_employee_unit_name_bng(g.getToOfficeUnitNameBangla())
                            .from_employee_unit_name_bng(g.getFromOfficeUnitNameBangla())
                            .from_employee_username(g.getFromGroUsername())
                            .from_employee_signature(null)
                            .created_at(String.valueOf(new Date() {{ String[] p=g.getCreatedAtEng().split("/"); int d=Integer.parseInt(p[0]),m=Integer.parseInt(p[1])-1,y=Integer.parseInt(p[2]); Calendar c=Calendar.getInstance(TimeZone.getTimeZone("UTC")); c.set(y,m,d,6,45,2); c.set(Calendar.MILLISECOND,0); setTime(c.getTimeInMillis()); } @Override public String toString(){ return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(this); }}))
                            .updated_at(null)
                            .created_by(null)
                            .modified_by(null)
                            .status(null)
                            .deadline_date(null)
                            .current_status(null)
                            .is_seen(null)
                            .assigned_role(g.getAssignedRole())
                            .complain_movement_attachment(g.getFiles())
                            .build()

            );
        }


        Map<String, Object> response = new HashMap<>();
        response.put("data", forwardingDTOList);
        response.put("status", "success");

        return response;
    }

    @GetMapping("/api/grievance/movement")
    public Map<String,Object> getMovement(
            Authentication authentication,
            @RequestParam("complaint_id") Long id
    ) throws ParseException {
        if (id == null){
            Map<String, Object> response = new HashMap<>();
            response.put("data", "Complaint could not be found");
            response.put("status", "error");

            return response;
        }
        List<GrievanceForwardingEmployeeRecordsDTO> grievanceList = grievanceForwardingService.getAllComplainantComplaintMovementHistoryByGrievance(id, authentication);
        List<MobileGrievanceForwardingDTO> forwardingDTOList = new ArrayList<>();

        for (GrievanceForwardingEmployeeRecordsDTO g : grievanceList){
            forwardingDTOList.add(
                    MobileGrievanceForwardingDTO.builder()
                            .id(null)
                            .complaint_id(Math.toIntExact(id))
                            .note(g.getComment())
                            .action(g.getAction())
                            .to_employee_record_id(null)
                            .from_employee_record_id(null)
                            .to_office_unit_organogram_id(null)
                            .from_office_unit_organogram_id(null)
                            .to_office_id(null)
                            .from_office_id(null)
                            .to_office_unit_id(null)
                            .from_office_unit_id(null)
                            .is_current(null)
                            .is_cc(g.getIsCC() ? 1 : 0)
                            .is_committee_head(g.getIsCommitteeHead() ? 1 : 0)
                            .is_committee_member(g.getIsCommitteeMember() ? 1 : 0)
                            .to_employee_name_bng(g.getToGroNameBangla())
                            .from_employee_name_bng(g.getFromGroNameBangla())
                            .to_employee_name_eng(g.getToGroNameEnglish())
                            .from_employee_name_eng(g.getFromGroNameEnglish())
                            .to_employee_designation_bng(g.getToDesignationNameBangla())
                            .from_employee_designation_bng(g.getFromDesignationNameBangla())
                            .to_office_name_bng(g.getToOfficeNameBangla())
                            .from_office_name_bng(g.getFromOfficeNameBangla())
                            .to_employee_unit_name_bng(g.getToOfficeUnitNameBangla())
                            .from_employee_unit_name_bng(g.getFromOfficeUnitNameBangla())
                            .from_employee_username(g.getFromGroUsername())
                            .from_employee_signature(null)
                            .created_at(String.valueOf(new Date() {{ String[] p=g.getCreatedAtEng().split("/"); int d=Integer.parseInt(p[0]),m=Integer.parseInt(p[1])-1,y=Integer.parseInt(p[2]); Calendar c=Calendar.getInstance(TimeZone.getTimeZone("UTC")); c.set(y,m,d,6,45,2); c.set(Calendar.MILLISECOND,0); setTime(c.getTimeInMillis()); } @Override public String toString(){ return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(this); }}))
                            .updated_at(null)
                            .created_by(null)
                            .modified_by(null)
                            .status(null)
                            .deadline_date(null)
                            .current_status(null)
                            .is_seen(null)
                            .assigned_role(g.getAssignedRole())
                            .complain_movement_attachment(g.getFiles())
                            .build()

            );
        }


        Map<String, Object> response = new HashMap<>();
        response.put("data", forwardingDTOList);
        response.put("status", "success");

        return response;
    }

    @GetMapping("/api/grievance/details")
    public Map<String, Object> getGrievanceDetails(
            Authentication authentication,
            @RequestParam("complaint_id") Long complaintId
    ) throws ParseException {
        MobileGrievanceResponseDTO grievance = mobileGrievanceService.findGrievancesById(complaintId);

        if (grievance == null){
            Map<String, Object> response = new HashMap<>();
            response.put("data", null);
            response.put("status", "success");

            return response;
        }

        Complainant complainant = complainantService.findOne(grievance.getComplainant_id());
        // Filter the occupations list to find the matching occupation ID

        SimpleDateFormat isoDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

        Map<String, Object> grievanceDetails = new HashMap<>();
        grievanceDetails.put("id", grievance.getId());
        grievanceDetails.put("submission_date", grievance.getSubmission_date());
        grievanceDetails.put("submission_date_bn", BanglaConverter.getDateBanglaFromEnglish(grievance.getSubmission_date()));
        grievanceDetails.put("complaint_type", grievance.getComplaint_type());
        grievanceDetails.put("current_status", grievance.getCurrent_status());
        grievanceDetails.put("subject", grievance.getSubject());
        grievanceDetails.put("details", grievance.getDetails());
        grievanceDetails.put("tracking_number", grievance.getTracking_number());
        grievanceDetails.put("tracking_number_bn", BanglaConverter.convertToBanglaDigit(grievance.getTracking_number()));
        grievanceDetails.put("complainant_id", grievance.getComplainant_id());
        grievanceDetails.put("office_id", grievance.getOffice_id());
        grievanceDetails.put("service_id", grievance.getService_id());
        grievanceDetails.put("service_id_before_forward", grievance.getService_id_before_forward());
        grievanceDetails.put("current_appeal_office_id", grievance.getCurrent_appeal_office_id());
        grievanceDetails.put("current_appeal_office_unit_organogram_id", grievance.getCurrent_appeal_office_unit_organogram_id());
        grievanceDetails.put("send_to_ao_office_id", grievance.getSend_to_ao_office_id());
        grievanceDetails.put("is_anonymous", grievance.getIs_anonymous());
        grievanceDetails.put("case_number", grievance.getCase_number());
        grievanceDetails.put("other_service", grievance.getOther_service());
        grievanceDetails.put("other_service_before_forward", grievance.getOther_service_before_forward());
        grievanceDetails.put("service_receiver", grievance.getService_receiver());
        grievanceDetails.put("service_receiver_relation", grievance.getService_receiver_relation());
        grievanceDetails.put("gro_decision", grievance.getGro_decision());
        grievanceDetails.put("gro_identified_complaint_cause", grievance.getGro_identified_complaint_cause());
        grievanceDetails.put("gro_suggestion", grievance.getGro_suggestion());
        grievanceDetails.put("ao_decision", grievance.getAo_decision());
        grievanceDetails.put("ao_identified_complaint_cause", grievance.getAo_identified_complaint_cause());
        grievanceDetails.put("ao_suggestion", grievance.getAo_suggestion());
        grievanceDetails.put("created_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(grievance.getCreated_at())));
        grievanceDetails.put("updated_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(grievance.getUpdated_at())));
        grievanceDetails.put("created_by", grievance.getCreated_by());
        grievanceDetails.put("modified_by", grievance.getModified_by());
        grievanceDetails.put("rating", grievance.getRating());
        grievanceDetails.put("appeal_rating", grievance.getAppeal_rating());
        grievanceDetails.put("is_rating_given", grievance.getIs_rating_given());
        grievanceDetails.put("is_appeal_rating_given", grievance.getIs_appeal_rating_given());
        grievanceDetails.put("feedback_comments", grievance.getFeedback_comments());
        grievanceDetails.put("appeal_feedback_comments", grievance.getAppeal_feedback_comments());
        grievanceDetails.put("source_of_grievance", grievance.getSource_of_grievance());
        grievanceDetails.put("status", grievance.getStatus());
        grievanceDetails.put("is_offline_complaint", grievance.getIs_offline_complaint());
        grievanceDetails.put("is_self_motivated_grievance", grievance.getIs_self_motivated_grievance());
        grievanceDetails.put("is_safety_net", grievance.getIs_safety_net());
        grievanceDetails.put("is_grs_user", grievance.getIs_grs_user());
        grievanceDetails.put("uploader_office_unit_organogram_id", grievance.getUploader_office_unit_organogram_id());
        grievanceDetails.put("complaint_category", grievance.getComplaint_category());
        grievanceDetails.put("sp_programme_id", grievance.getSp_programme_id());
        grievanceDetails.put("geo_division_id", grievance.getGeo_division_id());
        grievanceDetails.put("geo_district_id", grievance.getGeo_district_id());
        grievanceDetails.put("geo_upazila_id", grievance.getGeo_upazila_id());
        grievanceDetails.put("medium_of_submission", null);
        grievanceDetails.put("complaint_attachment_info", this.getComplainAttachments(grievance.getId()));
        grievanceDetails.put("mygov_user_id", null);
        grievanceDetails.put("triple_three_agent_id", null);
        grievanceDetails.put("grievance_from", grievance.getGrievance_from());
        grievanceDetails.put("possible_close_date", grievance.getPossible_close_date());
        grievanceDetails.put("possible_close_date_bn", grievance.getPossible_close_date_bn());
        grievanceDetails.put("is_evidence_provide", grievance.getIs_evidence_provide());
        grievanceDetails.put("is_see_hearing_date", grievance.getIs_see_hearing_date());


        Map<String, Object> complainantInfo = new HashMap<>();
        complainantInfo.put("id", complainant.getId());
        complainantInfo.put("name", complainant.getName());
        complainantInfo.put("identification_value", complainant.getIdentificationValue());
        complainantInfo.put("identification_type", complainant.getIdentificationType().toString());  // Assuming `IdentificationType` is an enum
        complainantInfo.put("mobile_number", complainant.getPhoneNumber());
        complainantInfo.put("email", complainant.getEmail());
        complainantInfo.put("birth_date", complainant.getBirthDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(complainant.getBirthDate()) : null);
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
        complainantInfo.put("is_authenticated", complainant.isAuthenticated() ? 1 : 0);
        complainantInfo.put("created_at", isoDateFormat.format(new Date(complainant.getCreatedAt().getTime())));
        complainantInfo.put("updated_at", isoDateFormat.format(new Date(complainant.getUpdatedAt().getTime())));
        complainantInfo.put("status", complainant.getStatus());
        complainantInfo.put("present_address_country_id", complainant.getPresentAddressCountryId());
        complainantInfo.put("permanent_address_country_id", complainant.getPermanentAddressCountryId());
        complainantInfo.put("is_blacklisted", complainantService.isBlacklistedUserByComplainantId(complainant.getId()) ? 1 : 0);
        List<Occupation> occupations = mobilePublicAPIService.getOccupationList();
        String complainantOccupation = complainant.getOccupation();
        String occupationId = occupations.stream()
                .filter(o -> complainantOccupation != null
                        && ((o.getOccupationBangla() != null && complainantOccupation.equals(o.getOccupationBangla()))
                        || (o.getOccupationEnglish() != null && complainantOccupation.equals(o.getOccupationEnglish()))))
                .map(o -> o.getId().toString())
                .findFirst()
                .orElse(null);
        complainantInfo.put("occupation", occupationId);

        List<Education> qualifications = mobilePublicAPIService.getQualificationList();
        String complainantQualification = complainant.getEducation();
        String qualificationId = qualifications.stream()
                .filter(q -> complainantQualification != null
                        && ((q.getEducationBangla() != null && complainantQualification.equals(q.getEducationBangla()))
                        || (q.getEducationEnglish() != null && complainantQualification.equals(q.getEducationEnglish()))))
                .map(q -> q.getId().toString())
                .findFirst()
                .orElse(null);
        complainantInfo.put("educational_qualification", qualificationId);
        complainantInfo.put("blacklister_office_id", null);
        complainantInfo.put("blacklister_office_name", null);
        complainantInfo.put("blacklist_reason", null);
        complainantInfo.put("is_requested", null);



//        Boolean isOccupation = false;
//        for (Occupation o : occupations){
//            if (complainant.getOccupation().equals(o.getOccupationBangla()) || complainant.getOccupation().equals(o.getOccupationEnglish())) {
//                complainantInfo.put("occupation", o.getId().toString());
//                isOccupation = true;
//                break;
//            }
//        }
//        if (!isOccupation) {
//            complainantInfo.put("occupation", null);
//        }



        Office office = officeRepo.findOfficeById(grievance.getOffice_id());

        Map<String, Object> officeInfo = new HashMap<>();
        officeInfo.put("id", grievance.getOffice_id());
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

    MobileComplainAttachmentInfoDTO getComplainAttachments(Long complainId) {
        List<FileDerivedDTO> complainAttachments = grievanceService.getGrievancesFiles(complainId);
        List<MobileComplainAttachmentInfoDTO> response = new ArrayList<>();
        for (FileDerivedDTO f : complainAttachments){
            response.add(MobileComplainAttachmentInfoDTO.builder()
                            .filePath(f.getUrl())
                            .fileTitle(f.getName())
                    .build());
        }
        return response.get(0);
    }

    @GetMapping("/api/grievance-track")
    public MobileResponse getGrievanceByTrackingNumber(
            @RequestParam("tracking_number") String trx
    ){
        List<MobileGrievanceResponseDTO> grievanceList = mobileGrievanceService.findGrievancesByTrackingNumber(trx);

        if (grievanceList == null || grievanceList.isEmpty()){
            return MobileResponse.builder()
                    .status("empty")
                    .data(null)
                    .build();
        }
        return MobileResponse.builder()
                .status("success")
                .data(grievanceList.get(0))
                .build();
    }


    @RequestMapping(value = "/api/grievance/list/to-employee", method = RequestMethod.GET)
    public Map<String, Object> getToEmployeeGrievances(Authentication authentication,
                                                       @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.NORMAL_INBOX);
    }

    @RequestMapping(value = "/api/grievance/list/from-employee", method = RequestMethod.GET)
    public Map<String, Object> getFromEmployeeGrievances(Authentication authentication,
                                                     @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.NORMAL_OUTBOX);
    }

    @RequestMapping(value = "/api/grievance/list/closed_grievances", method = RequestMethod.GET)
    public Map<String, Object> getResolvedGrievances(Authentication authentication,
                                                         @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.NORMAL_CLOSED);
    }

    @RequestMapping(value = "/api/grievance/list/forwarded_to_other_office", method = RequestMethod.GET)
    public Map<String, Object> getForwardedGrievances(Authentication authentication,
                                                     @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.NORMAL_FORWARDED);
    }

    @RequestMapping(value = "/api/grievance/list/expired_grievances", method = RequestMethod.GET)
    public Map<String, Object> getExpiredGrievances(Authentication authentication,
                                                      @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.NORMAL_EXPIRED);
    }

    @RequestMapping(value = "/api/grievance/list/cc", method = RequestMethod.GET)
    public Map<String, Object> getCC(Authentication authentication,
                                                    @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.NORMAL_CC);
    }

    @RequestMapping(value = "/api/grievance/list/incoming-appeal", method = RequestMethod.GET)
    public Map<String, Object> getIncomingAppeals(Authentication authentication,
                                                  @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.APPEAL_INBOX);
    }
    @RequestMapping(value = "/api/grievance/list/closed-appeal", method = RequestMethod.GET)
    public Map<String, Object> getClosedAppeals(Authentication authentication,
                                                  @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.APPEAL_CLOSED);
    }
    @RequestMapping(value = "/api/grievance/list/sent-appeal", method = RequestMethod.GET)
    public Map<String, Object> getSentAppeal(Authentication authentication,
                                                  @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.APPEAL_OUTBOX);
    }
}
