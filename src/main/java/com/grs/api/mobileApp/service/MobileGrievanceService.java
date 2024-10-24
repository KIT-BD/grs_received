package com.grs.api.mobileApp.service;

import com.grs.api.mobileApp.dto.MobileGrievanceResponseDTO;
import com.grs.api.model.UserType;
import com.grs.api.model.request.FileDTO;
import com.grs.api.model.request.GrievanceWithoutLoginRequestDTO;
import com.grs.api.model.response.file.FileBaseDTO;
import com.grs.api.model.response.file.FileContainerDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.core.dao.GrievanceDAO;
import com.grs.core.domain.ServiceType;
import com.grs.core.domain.grs.ServiceOrigin;
import com.grs.core.service.GrievanceService;
import com.grs.core.service.OfficeService;
import com.grs.core.service.StorageService;
import com.grs.utils.BanglaConverter;
import com.grs.utils.DateTimeConverter;
import com.grs.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.Grievance;
import com.grs.core.repo.grs.GrievanceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.List;

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

    public MobileGrievanceResponseDTO saveGrievanceWithLogin(
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
                .serviceOthers(null)
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
        String closedDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssa").format(new Date(g.getSubmissionDate().getTime() + (30L * 24 * 60 * 60 * 1000)));

        return MobileGrievanceResponseDTO.builder()
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
                .is_grs_user(g.isGrsUser())
                .office_id(g.getOfficeId())
                .is_self_motivated_grievance(g.getIsSelfMotivatedGrievance())
                .other_service(g.getOtherService())
                .service_id(Optional.ofNullable(g.getServiceOrigin()).map(ServiceOrigin::getId).orElse(null))
                .source_of_grievance(g.getSourceOfGrievance())
                .status(String.valueOf(g.getStatus()))
                .possible_close_date(closedDate)
                .possible_close_date_bn(BanglaConverter.getDateBanglaFromEnglish(closedDate))
                .created_at(String.valueOf(g.getCreatedAt()))
                .updated_at(String.valueOf(g.getUpdatedAt()))
                .build();
    }

    public MobileGrievanceResponseDTO savePublicGrievanceService(
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
        String closedDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssa").format(new Date(g.getSubmissionDate().getTime() + (30L * 24 * 60 * 60 * 1000)));

        return MobileGrievanceResponseDTO.builder()
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
                .is_grs_user(g.isGrsUser())
                .office_id(g.getOfficeId())
                .is_self_motivated_grievance(g.getIsSelfMotivatedGrievance())
                .other_service(g.getOtherService())
                .service_id(Optional.ofNullable(g.getServiceOrigin()).map(ServiceOrigin::getId).orElse(null))
                .source_of_grievance(g.getSourceOfGrievance())
                .status(String.valueOf(g.getStatus()))
                .possible_close_date(closedDate)
                .possible_close_date_bn(BanglaConverter.getDateBanglaFromEnglish(closedDate))
                .created_at(String.valueOf(g.getCreatedAt()))
                .updated_at(String.valueOf(g.getUpdatedAt()))
                .build();
        //return grievanceService.addGrievanceWithoutLogin(null, grievanceDTO);
    }
    public List<MobileGrievanceResponseDTO> findGrievancesByUser(
            Long complainantId){
        List<Grievance> grievances = grievanceRepo.findGrievancesByComplainantId(complainantId);
        List<MobileGrievanceResponseDTO> grievanceDTOList = new ArrayList<>();

        for (Grievance g : grievances) {
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
        return grievanceDTOList;
    }

    public List<MobileGrievanceResponseDTO> findGrievancesByTrackingNumber(String trx) {

        List<Grievance> grievances = grievanceRepo.findGrievancesByTrackingNumber(trx);
        List<MobileGrievanceResponseDTO> grievanceDTOList = new ArrayList<>();

        for (Grievance g : grievances) {
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
        return grievanceDTOList;
    }
}
