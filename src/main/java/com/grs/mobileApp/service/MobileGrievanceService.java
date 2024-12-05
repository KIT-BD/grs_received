package com.grs.mobileApp.service;

import com.grs.api.model.UserInformation;
import com.grs.api.model.response.grievance.GrievanceDTO;
import com.grs.core.domain.grs.*;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.model.ListViewType;
import com.grs.core.repo.projapoti.OfficeRepo;
import com.grs.core.service.ComplainantService;
import com.grs.mobileApp.dto.MobileComplainAttachmentInfoDTO;
import com.grs.mobileApp.dto.MobileGrievanceResponseDTO;
import com.grs.mobileApp.dto.MobileGrievanceSubmissionResponseDTO;
import com.grs.api.model.UserType;
import com.grs.api.model.request.FileDTO;
import com.grs.api.model.request.GrievanceWithoutLoginRequestDTO;
import com.grs.api.model.response.file.FileBaseDTO;
import com.grs.api.model.response.file.FileContainerDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.core.dao.GrievanceDAO;
import com.grs.core.domain.ServiceType;
import com.grs.core.service.GrievanceService;
import com.grs.core.service.OfficeService;
import com.grs.core.service.StorageService;
import com.grs.mobileApp.dto.MobileResponse;
import com.grs.utils.BanglaConverter;
import com.grs.utils.DateTimeConverter;
import com.grs.utils.ListViewConditionOnCurrentStatusGenerator;
import com.grs.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.grs.core.repo.grs.GrievanceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MobileGrievanceService {

    @Autowired
    private GrievanceService grievanceService;
    @Autowired
    private StorageService storageService;
    private final GrievanceRepo grievanceRepo;
    private final GrievanceDAO grievanceDAO;
    private final OfficeService officeService;
    private final ComplainantService complainantService;
    private final OfficeRepo officeRepo;
    private final MobilePublicAPIService mobilePublicAPIService;

    public MobileGrievanceSubmissionResponseDTO saveGrievanceWithLogin(
            Authentication authentication,
            Complainant complainant,
            Long officeId,
            String serviceId,
            String description,
            String subject,
            Boolean isGrsUser,
            String fileNameByUser,
            List<MultipartFile> files,
            Principal principal
    ) throws Exception {
        GrievanceWithoutLoginRequestDTO requestDTO = GrievanceWithoutLoginRequestDTO.builder()
                .subject(subject)
                .complainantPhoneNumber(complainant.getPhoneNumber())
                .name(complainant.getName())
                .email(complainant.getEmail())
                .officeId(String.valueOf(officeId))
                .officeLayers(String.valueOf(officeService.findOne(officeId).getOfficeLayer().getId()))
                .serviceId(serviceId == null || serviceId.trim().isEmpty() ? "0" : serviceId)
                .submissionDate(DateTimeConverter.convertDateToString(new Date()))
                .body(description)
                .relation(null)
                .serviceReceiver(null)
                .serviceOthers("অন্যান্য")
                .isAnonymous(false)
                .serviceType(ServiceType.NAGORIK)
                .offlineGrievanceUpload(false)
                .PhoneNumber(null)
                .isSelfMotivated(null)
                .SourceOfGrievance(null)
                .user(null)
                .secret(null)
                .submittedThroughApi(0)
                .grievanceCategory(ServiceType.NAGORIK.ordinal())
                .spProgrammeId(null)
                .division(null)
                .district(null)
                .upazila(null)
                .safetyNetId(0)
                .divisionId(complainant.getPermanentAddressDivisionId() == null ? 0 : complainant.getPermanentAddressDivisionId())
                .districtId(complainant.getPermanentAddressDistrictId() == null ? 0 : complainant.getPermanentAddressDistrictId())
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
            }
            requestDTO.setFiles(fileDTOS);
        }

        WeakHashMap<String, Object> addedGrievance = grievanceService.addGrievanceForOthers(authentication, requestDTO);

        String trackingNumber = addedGrievance.get("trackingNumber").toString();
        Grievance g = grievanceDAO.findByTrackingNumber(trackingNumber);

        String submissionDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssa").format(new Date(g.getSubmissionDate().getTime()));
        String closedDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(g.getSubmissionDate().getTime() + (30L * 24 * 60 * 60 * 1000)));

        return MobileGrievanceSubmissionResponseDTO.builder()
                .id(g.getId())
                .subject(g.getSubject())
                .submission_date(submissionDate)
                .submission_date_bn(BanglaConverter.getDateBanglaFromEnglish(submissionDate))
                .complaint_type(String.valueOf(g.getGrievanceType()))
                .complaint_type_bn(BanglaConverter.convertServiceTypeToBangla(g.getGrievanceType()))
                .current_status(String.valueOf(g.getGrievanceCurrentStatus()))
                .current_status_bn(BanglaConverter.convertGrievanceStatusToBangla(g.getGrievanceCurrentStatus()))
                .details(g.getDetails())
                .tracking_number(g.getTrackingNumber())
                .tracking_number_bn(g.getTrackingNumber())
                .complainant_id(g.getComplainantId())
                .is_grs_user(g.isGrsUser() ? 1L : 0L)
                .office_id(g.getOfficeId())
                .is_self_motivated_grievance(g.getIsSelfMotivatedGrievance() ? 1L : 0L)
                .other_service(g.getOtherService())
                .service_id(Optional.ofNullable(g.getServiceOrigin()).map(ServiceOrigin::getId).orElse(null))
                .source_of_grievance(g.getSourceOfGrievance())
                .status(g.getStatus() ? 1L : 0L)
                .possible_close_date(closedDate)
                .possible_close_date_bn(BanglaConverter.getDateBanglaFromEnglish(closedDate))
                .created_at(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(g.getCreatedAt()))
                .updated_at(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(g.getUpdatedAt()))
                .build();
    }

    public MobileGrievanceSubmissionResponseDTO savePublicGrievanceService(
            String officeId, String description, String subject,
            String spProgrammeId, String mobileNumber, String name,
            String email, Integer divisionId, Integer districtId,
            Integer upazilaId, Integer complaintCategory,
            String fileNameByUser, List<MultipartFile> files,
            Principal principal) throws Exception {

        // Create an object of GrievanceWithoutLoginRequestDTO
        GrievanceWithoutLoginRequestDTO grievanceDTO = new GrievanceWithoutLoginRequestDTO();

        // Set dummy data
        grievanceDTO.setComplainantPhoneNumber(mobileNumber);
        grievanceDTO.setName(name);
        grievanceDTO.setEmail(email);
        grievanceDTO.setOfficeId(officeId);
        grievanceDTO.setOfficeLayers(StringUtil.isValidString(officeId) ? String.valueOf(officeService.findOne(Long.parseLong(officeId)).getOfficeLayer().getId()) : null);
        grievanceDTO.setServiceId("0");
        grievanceDTO.setSubmissionDate(DateTimeConverter.convertDateToString(new Date()));
        grievanceDTO.setSubject(subject);
        grievanceDTO.setBody(description);
        grievanceDTO.setRelation(null);
        grievanceDTO.setServiceReceiver(null);
        grievanceDTO.setServiceOthers("অন্যান্য");
        grievanceDTO.setIsAnonymous(false);
        grievanceDTO.setServiceType(ServiceType.NAGORIK);
        grievanceDTO.setOfflineGrievanceUpload(false);
        grievanceDTO.setPhoneNumber(null);
        grievanceDTO.setIsSelfMotivated(false);
        grievanceDTO.setSourceOfGrievance(UserType.COMPLAINANT.toString());
        grievanceDTO.setUser(null);
        grievanceDTO.setSecret(null);
        grievanceDTO.setSubmittedThroughApi(0);
        grievanceDTO.setGrievanceCategory(complaintCategory);
        grievanceDTO.setSpProgrammeId(spProgrammeId);
        grievanceDTO.setDivision(divisionId != null ? divisionId.toString() : null);
        grievanceDTO.setDistrict(districtId != null ? districtId.toString() : null);
        grievanceDTO.setUpazila(upazilaId != null ? upazilaId.toString() : null);
        grievanceDTO.setSafetyNetId(0);
        grievanceDTO.setDivisionId(divisionId != null ? divisionId : 0);
        grievanceDTO.setDistrictId(districtId != null ? districtId : 0);
        grievanceDTO.setUpazilaId(upazilaId != null ? upazilaId : 0);

        if (files != null && !files.isEmpty()) {
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
                // System.out.println("Name: " + fileNames[i-1]);
                // System.out.println("URL: " + fileDerivedDTO.getUrl());
            }
            grievanceDTO.setFiles(fileDTOS);
        }

        WeakHashMap<String, Object> addedGrievance = grievanceService.addGrievanceWithoutLogin(null, grievanceDTO);

        System.out.println("Added Grievance: " + addedGrievance);

        String trackingNumber = addedGrievance.get("trackingNumber").toString();
        Grievance g = grievanceDAO.findByTrackingNumber(trackingNumber);

        String submissionDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssa").format(new Date(g.getSubmissionDate().getTime()));
        String closedDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(g.getSubmissionDate().getTime() + (30L * 24 * 60 * 60 * 1000)));

        return MobileGrievanceSubmissionResponseDTO.builder()
                .id(g.getId())
                .subject(g.getSubject())
                .submission_date(submissionDate)
                .submission_date_bn(BanglaConverter.getDateBanglaFromEnglish(submissionDate))
                .complaint_type(String.valueOf(g.getGrievanceType()))
                .complaint_type_bn(BanglaConverter.convertServiceTypeToBangla(g.getGrievanceType()))
                .current_status(String.valueOf(g.getGrievanceCurrentStatus()))
                .current_status_bn(BanglaConverter.convertGrievanceStatusToBangla(g.getGrievanceCurrentStatus()))
                .details(g.getDetails())
                .tracking_number(g.getTrackingNumber())
                .tracking_number_bn(BanglaConverter.convertToBanglaDigit(g.getTrackingNumber()))
                .complainant_id(g.getComplainantId())
                .is_grs_user(g.isGrsUser() ? 1L : 0L)
                .office_id(g.getOfficeId())
                .is_self_motivated_grievance(g.getIsSelfMotivatedGrievance() ? 1L : 0L)
                .other_service(g.getOtherService())
                .service_id(Optional.ofNullable(g.getServiceOrigin()).map(ServiceOrigin::getId).orElse(null))
                .source_of_grievance(g.getSourceOfGrievance())
                .status(g.getStatus() ? 1L : 0L)
                .possible_close_date(closedDate)
                .possible_close_date_bn(BanglaConverter.getDateBanglaFromEnglish(closedDate))
                .created_at(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(g.getCreatedAt()))
                .updated_at(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(g.getUpdatedAt()))
                .build();
        //return grievanceService.addGrievanceWithoutLogin(null, grievanceDTO);
    }

    public Map<String,Object> getGrievanceDetails(MobileGrievanceResponseDTO grievance) throws ParseException {
        Map<String, Object> grievanceDetails = new LinkedHashMap<>();
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

        return grievanceDetails;
    }

    public Map<String,Object> getComplaintDetailsById(Long complaintId) throws ParseException {

        MobileGrievanceResponseDTO grievance = findGrievancesById(complaintId);

        if (grievance == null){
            Map<String, Object> response = new HashMap<>();
            response.put("data", null);
            response.put("status", "success");

            return response;
        }

        Complainant complainant = complainantService.findOne(grievance.getComplainant_id());
        // Filter the occupations list to find the matching occupation ID

        SimpleDateFormat isoDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

        Map<String,Object> grievanceDetails = this.getGrievanceDetails(grievance);

        Map<String, Object> complainantInfo = new HashMap<>();

        if (complainant != null) {
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
        }


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

    public MobileGrievanceResponseDTO findGrievancesById(
            Long complainantId){
        Grievance g = grievanceRepo.findOne(complainantId);

        if (g == null){
            return null;
        }

        return MobileGrievanceResponseDTO.builder()
                .id(g.getId())
                .submission_date(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(g.getSubmissionDate()))
                .submission_date_bn(BanglaConverter.getDateBanglaFromEnglish(String.valueOf(g.getSubmissionDate())))
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
                .is_grs_user(g.isGrsUser() ? 1L : 0)
                .office_id(g.getOfficeId())
                .service_id(Optional.ofNullable(g.getServiceOrigin()).map(ServiceOrigin::getId).orElse(null))
                .service_id_before_forward(Optional.ofNullable(g.getServiceOriginBeforeForward()).map(ServiceOrigin::getId).orElse(null))
                .current_appeal_office_id(g.getCurrentAppealOfficeId())
                .current_appeal_office_unit_organogram_id(g.getCurrentAppealOfficerOfficeUnitOrganogramId())
                .send_to_ao_office_id(g.getSendToAoOfficeId())
                .is_anonymous(g.isAnonymous() ? 1L : 0)
                .case_number(Optional.ofNullable(g.getCaseNumber()).map(String::valueOf).orElse(null))
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
                .created_at(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(g.getCreatedAt()))
                .updated_at(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(g.getUpdatedAt()))
                .created_by(g.getCreatedBy())
                .modified_by(g.getModifiedBy())
                .rating(Optional.ofNullable(g.getRating()).map(String::valueOf).orElse(null))
                .appeal_rating(Optional.ofNullable(g.getAppealRating()).map(String::valueOf).orElse(null))
                .status(g.getStatus() != null && g.getStatus() ? 1L : 0L)
                .is_rating_given(g.getIsRatingGiven() != null && g.getIsRatingGiven() ? 1L : 0L)
                .is_appeal_rating_given(g.getIsAppealRatingGiven() != null && g.getIsAppealRatingGiven() ? 1L : 0L)
                .is_self_motivated_grievance(g.getIsSelfMotivatedGrievance() != null && g.getIsSelfMotivatedGrievance() ? 1L : 0L)
                .is_offline_complaint(g.getIsOfflineGrievance() != null && g.getIsOfflineGrievance() ? 1L : 0L)
                .feedback_comments(g.getFeedbackComments())
                .appeal_feedback_comments(g.getAppealFeedbackComments())
                .source_of_grievance(g.getSourceOfGrievance())
                .uploader_office_unit_organogram_id(Optional.ofNullable(g.getUploaderOfficeUnitOrganogramId()).map(String::valueOf).orElse(null))
                .possible_close_date(null)
                .possible_close_date_bn(null)
                .is_evidence_provide(null)
                .is_see_hearing_date(null)
                .is_safety_net(g.isSafetyNet() ? 1L : 0)
                .complaint_category(Optional.ofNullable(g.getComplaintCategory()).map(Long::valueOf).orElse(null))
                .sp_programme_id(Optional.ofNullable(g.getSpProgrammeId()).map(Long::valueOf).orElse(null))
                .geo_division_id(Optional.ofNullable(g.getGeoDivisionId()).map(Long::valueOf).orElse(null))
                .geo_district_id(Optional.ofNullable(g.getGeoDistrictId()).map(Long::valueOf).orElse(null))
                .geo_upazila_id(Optional.ofNullable(g.getGeoUpazilaId()).map(Long::valueOf).orElse(null))
                .build();

    }
    public List<MobileGrievanceResponseDTO> findGrievancesByUser(
            Long complainantId) throws ParseException {
        List<Grievance> grievances = grievanceRepo.findGrievancesByComplainantId(complainantId);
        List<MobileGrievanceResponseDTO> grievanceDTOList = new ArrayList<>();

        for (Grievance g : grievances) {
            grievanceDTOList.add(
                    MobileGrievanceResponseDTO.builder()
                            .id(g.getId())
                            .submission_date(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(g.getSubmissionDate()))
                            .submission_date_bn(BanglaConverter.getDateBanglaFromEnglish(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(g.getSubmissionDate())))
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
                            .office_id(g.getOfficeId())
                            .service_id(Optional.ofNullable(g.getServiceOrigin()).map(ServiceOrigin::getId).orElse(null))
                            .service_id_before_forward(Optional.ofNullable(g.getServiceOriginBeforeForward()).map(ServiceOrigin::getId).orElse(null))
                            .current_appeal_office_id(g.getCurrentAppealOfficeId())
                            .current_appeal_office_unit_organogram_id(g.getCurrentAppealOfficerOfficeUnitOrganogramId())
                            .send_to_ao_office_id(g.getSendToAoOfficeId())
                            .case_number(Optional.ofNullable(g.getCaseNumber()).map(String::valueOf).orElse(null))
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
                            .created_at(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(g.getCreatedAt()))
                            .updated_at(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(g.getUpdatedAt()))
                            .created_by(g.getCreatedBy())
                            .modified_by(g.getModifiedBy())
                            .rating(Optional.ofNullable(g.getRating()).map(String::valueOf).orElse(null))
                            .appeal_rating(Optional.ofNullable(g.getAppealRating()).map(String::valueOf).orElse(null))
                            .is_rating_given(g.getIsRatingGiven() != null && g.getIsRatingGiven() ? 1L : 0L)
                            .is_grs_user(g.isGrsUser() ? 1L : 0L)
                            .is_anonymous(g.isAnonymous() ? 1L : 0L)
                            .is_safety_net(g.isSafetyNet() ? 1L : 0)
                            .status(g.getStatus() != null && g.getStatus() ? 1L : 0)
                            .is_offline_complaint(g.getIsOfflineGrievance() != null && g.getIsOfflineGrievance() ? 1L : 0)
                            .is_appeal_rating_given(g.getIsAppealRatingGiven() != null && g.getIsAppealRatingGiven() ? 1L : 0)
                            .is_self_motivated_grievance(g.getIsSelfMotivatedGrievance() != null && g.getIsSelfMotivatedGrievance() ? 1L : 0)
                            .feedback_comments(g.getFeedbackComments())
                            .appeal_feedback_comments(g.getAppealFeedbackComments())
                            .source_of_grievance(g.getSourceOfGrievance())
                            .uploader_office_unit_organogram_id(Optional.ofNullable(g.getUploaderOfficeUnitOrganogramId()).map(String::valueOf).orElse(null))
                            .possible_close_date(null)
                            .possible_close_date_bn(null)
                            .is_evidence_provide(null)
                            .is_see_hearing_date(null)
                            .complaint_category(Optional.ofNullable(g.getComplaintCategory()).map(Long::valueOf).orElse(null))
                            .sp_programme_id(Optional.ofNullable(g.getSpProgrammeId()).map(Long::valueOf).orElse(null))
                            .geo_division_id(Optional.ofNullable(g.getGeoDivisionId()).map(Long::valueOf).orElse(null))
                            .geo_district_id(Optional.ofNullable(g.getGeoDistrictId()).map(Long::valueOf).orElse(null))
                            .geo_upazila_id(Optional.ofNullable(g.getGeoUpazilaId()).map(Long::valueOf).orElse(null))
                            .build()
            );
        }
        return grievanceDTOList;
    }

    public List<MobileGrievanceResponseDTO> findGrievancesByTrackingNumber(String trx) {

        List<Grievance> grievances = grievanceRepo.findGrievancesByTrackingNumber(trx);
        List<MobileGrievanceResponseDTO> grievanceDTOList = new ArrayList<>();

        for (Grievance g : grievances) {
            grievanceDTOList.add(
                    MobileGrievanceResponseDTO.builder()
                            .id(g.getId())
                            .submission_date(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(g.getSubmissionDate()))
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
                            .office_id(g.getOfficeId())
                            .service_id(Optional.ofNullable(g.getServiceOrigin()).map(ServiceOrigin::getId).orElse(null))
                            .service_id_before_forward(Optional.ofNullable(g.getServiceOriginBeforeForward()).map(ServiceOrigin::getId).orElse(null))
                            .current_appeal_office_id(g.getCurrentAppealOfficeId())
                            .current_appeal_office_unit_organogram_id(g.getCurrentAppealOfficerOfficeUnitOrganogramId())
                            .send_to_ao_office_id(g.getSendToAoOfficeId())
                            .case_number(Optional.ofNullable(g.getCaseNumber()).map(String::valueOf).orElse(null))
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
                            .created_at(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(g.getCreatedAt()))
                            .updated_at(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(g.getUpdatedAt()))
                            .created_by(g.getCreatedBy())
                            .modified_by(g.getModifiedBy())
                            .rating(Optional.ofNullable(g.getRating()).map(String::valueOf).orElse(null))
                            .appeal_rating(Optional.ofNullable(g.getAppealRating()).map(String::valueOf).orElse(null))
                            .feedback_comments(g.getFeedbackComments())
                            .appeal_feedback_comments(g.getAppealFeedbackComments())
                            .source_of_grievance(g.getSourceOfGrievance())
                            .status(g.getStatus() ? 1L : 0)
                            .is_grs_user(g.isGrsUser() ? 1L : 0)
                            .is_anonymous(g.isAnonymous() ? 1L : 0)
                            .is_safety_net(g.isSafetyNet() ? 1L : 0)
                            .is_rating_given(g.getIsRatingGiven() != null && g.getIsRatingGiven() ? 1L : 0)
                            .is_offline_complaint(g.getIsOfflineGrievance() != null && g.getIsOfflineGrievance() ? 1L : 0)
                            .is_appeal_rating_given(g.getIsAppealRatingGiven() != null && g.getIsAppealRatingGiven() ? 1L : 0)
                            .is_self_motivated_grievance(g.getIsSelfMotivatedGrievance() != null && g.getIsSelfMotivatedGrievance() ? 1L : 0)
                            .uploader_office_unit_organogram_id(Optional.ofNullable(g.getUploaderOfficeUnitOrganogramId()).map(String::valueOf).orElse(null))
                            .possible_close_date(null)
                            .possible_close_date_bn(null)
                            .is_evidence_provide(null)
                            .is_see_hearing_date(null)
                            .complaint_category(Optional.ofNullable(g.getComplaintCategory()).map(Long::valueOf).orElse(null))
                            .sp_programme_id(Optional.ofNullable(g.getSpProgrammeId()).map(Long::valueOf).orElse(null))
                            .geo_division_id(Optional.ofNullable(g.getGeoDivisionId()).map(Long::valueOf).orElse(null))
                            .geo_district_id(Optional.ofNullable(g.getGeoDistrictId()).map(Long::valueOf).orElse(null))
                            .geo_upazila_id(Optional.ofNullable(g.getGeoUpazilaId()).map(Long::valueOf).orElse(null))
                            .build()
            );
        }
        return grievanceDTOList;
    }

    public Map<String, Object> findGrievances(UserInformation userInformation, Pageable pageable, ListViewType listViewType) {
        Page<GrievanceDTO> listViewWithSearching = grievanceService.getListViewWithSearching(
                userInformation, "", listViewType, pageable
        );

        List<GrievanceDTO> grievanceDTOList = listViewWithSearching.getContent();
        Integer noOfPages = listViewWithSearching.getTotalPages();

        List<MobileGrievanceResponseDTO> grievanceResponseList = grievanceDTOList.stream()
                .map(this::mapGrievanceDTOToMobileResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", grievanceResponseList);
        response.put("noOfPages", noOfPages);

        return response;
    }


    public MobileGrievanceResponseDTO mapGrievanceDTOToMobileResponse(GrievanceDTO grievanceDTO) {

//        String regex = "\\(([^)]+)\\)";
//        Pattern pattern = Pattern.compile(regex);

        MobileGrievanceResponseDTO mobileGrievanceResponseDTO = MobileGrievanceResponseDTO
                .builder()
                .id(Long.parseLong(grievanceDTO.getId()))
                .submission_date(
                        Optional.ofNullable(grievanceDTO.getSubmissionDateEnglish())
                                .map(date -> {
                                    try {
                                        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                                .format(new SimpleDateFormat("dd-MM-yyyy hh:mm:ss:a").parse(date));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        return null;
                                    }
                                })
                                .orElse(null)
                )
                .submission_date_bn(grievanceDTO.getSubmissionDateBangla())
                .complaint_type(grievanceDTO.getTypeEnglish())
                .complaint_type_bn(grievanceDTO.getTypeBangla())
                .current_status(extractOutofParenthesis(grievanceDTO.getStatusEnglish()))
                .current_status_bn(extractOutofParenthesis(grievanceDTO.getStatusBangla()))
                .subject(grievanceDTO.getSubject())
                .details(null)
                .grievance_from(null)
                .tracking_number(grievanceDTO.getTrackingNumberEnglish())
                .tracking_number_bn(grievanceDTO.getTrackingNumberBangla())
                .complainant_id(null)
                .mygov_user_id(null)
                .triple_three_agent_id(null)
                .is_grs_user(null)
                .office_id(null)
                .service_id(null)
                .service_id_before_forward(null)
                .current_appeal_office_id(null)
                .current_appeal_office_unit_organogram_id(null)
                .send_to_ao_office_id(null)
                .is_anonymous(null)
                .case_number(grievanceDTO.getCaseNumberEnglish() != null && !grievanceDTO.getCaseNumberEnglish().isEmpty() ? grievanceDTO.getCaseNumberEnglish() : null)
                .other_service(null)
                .other_service_before_forward(null)
                .service_receiver(null)
                .service_receiver_relation(null)
                .gro_decision(null)
                .gro_identified_complaint_cause(null)
                .gro_suggestion(null)
                .ao_decision(null)
                .ao_identified_complaint_cause(null)
                .ao_suggestion(null)
                .created_at(null)
                .updated_at(null)
                .created_by(null)
                .modified_by(null)
                .status(null)
                .rating(grievanceDTO.getRating() != null ? String.valueOf(grievanceDTO.getRating()) : null)
                .appeal_rating(grievanceDTO.getAppealRating() != null ? String.valueOf(grievanceDTO.getAppealRating()) : null)
                .is_rating_given(null)
                .is_appeal_rating_given(null)
                .feedback_comments(grievanceDTO.getFeedbackComments() != null ? grievanceDTO.getFeedbackComments() : null)
                .appeal_feedback_comments(grievanceDTO.getAppealFeedbackComments() != null ? grievanceDTO.getAppealFeedbackComments() : null)
                .source_of_grievance(null)
                .is_offline_complaint(null)
                .is_self_motivated_grievance(null)
                .uploader_office_unit_organogram_id(null)
                .possible_close_date(
                        Optional.ofNullable(grievanceDTO.getExpectedDateOfClosingEnglish())
                                .map(date -> {
                                    try {
                                        return new SimpleDateFormat("yyyy-MM-dd")
                                                .format(new SimpleDateFormat("dd-MM-yyyy hh:mm:ss:a").parse(date));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        return null;
                                    }
                                })
                                .orElse(null)
                )
                .possible_close_date_bn(
                        BanglaConverter.getDateBanglaFromEnglish(Optional.ofNullable(grievanceDTO.getExpectedDateOfClosingEnglish())
                                .map(date -> {
                                    try {
                                        return new SimpleDateFormat("yyyy-MM-dd")
                                                .format(new SimpleDateFormat("dd-MM-yyyy hh:mm:ss:a").parse(date));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        return null;
                                    }
                                })
                                .orElse(null))
                )
                .is_evidence_provide(null)
                .is_see_hearing_date(null)
                .is_safety_net(grievanceDTO.isSafetyNet() ? 1L : 0)
                .complaint_category(grievanceDTO.getComplaintCategoryDetails() != null && grievanceDTO.getComplaintCategoryDetails().matches("-?\\d+") ? Long.parseLong(grievanceDTO.getComplaintCategoryDetails()) : null)
                .sp_programme_id(null)
                .geo_division_id(null)
                .geo_district_id(null)
                .geo_upazila_id(null)
                .build();
        return mobileGrievanceResponseDTO;
    }

    private String extractOutofParenthesis(String status) {
        if (status == null || !status.contains("(") || !status.contains(")")) {
            return null;
        }

        try {
            String[] firstGone = status.split("\\(");
            return firstGone[1].split("\\)")[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    List<MobileComplainAttachmentInfoDTO> getComplainAttachments(Long complainId) {
        List<FileDerivedDTO> complainAttachments = grievanceService.getGrievancesFiles(complainId);
        List<MobileComplainAttachmentInfoDTO> response = new ArrayList<>();
        for (FileDerivedDTO f : complainAttachments){
            response.add(MobileComplainAttachmentInfoDTO.builder()
                    .file_path(f.getUrl())
                    .file_title(f.getName())
                    .file_type(f.getUrl().substring(f.getUrl().lastIndexOf('.') + 1).toUpperCase().replaceAll("/$", ""))
                    .build());
        }
        return response;
    }
}
