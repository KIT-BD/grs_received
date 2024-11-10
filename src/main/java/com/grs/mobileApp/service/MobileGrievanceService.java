package com.grs.mobileApp.service;

import com.grs.api.model.UserInformation;
import com.grs.api.model.response.grievance.GrievanceDTO;
import com.grs.core.model.ListViewType;
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
import com.grs.core.domain.grs.ServiceOrigin;
import com.grs.core.service.GrievanceService;
import com.grs.core.service.OfficeService;
import com.grs.core.service.StorageService;
import com.grs.mobileApp.dto.MobileResponse;
import com.grs.utils.BanglaConverter;
import com.grs.utils.DateTimeConverter;
import com.grs.utils.ListViewConditionOnCurrentStatusGenerator;
import com.grs.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.Grievance;
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

    public Map<String, Object> findOutboxGrievances(UserInformation userInformation, Pageable pageable) {
        Page<GrievanceDTO> listViewWithSearching = grievanceService.getListViewWithSearching(
                userInformation, null, ListViewType.NORMAL_OUTBOX, pageable
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
        MobileGrievanceResponseDTO mobileGrievanceResponseDTO = MobileGrievanceResponseDTO
                .builder()
                .id(Long.parseLong(grievanceDTO.getId()))
                .submission_date(grievanceDTO.getSubmissionDateEnglish())
                .submission_date_bn(grievanceDTO.getSubmissionDateBangla())
                .complaint_type(grievanceDTO.getTypeEnglish())
                .complaint_type_bn(grievanceDTO.getTypeBangla())
                .current_status(grievanceDTO.getStatusEnglish())
                .current_status_bn(grievanceDTO.getStatusBangla())
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
                .case_number(grievanceDTO.getCaseNumberEnglish() != null && !grievanceDTO.getCaseNumberEnglish().isEmpty() ? Long.parseLong(grievanceDTO.getCaseNumberEnglish()) : null)
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
                .possible_close_date(grievanceDTO.getExpectedDateOfClosingEnglish())
                .possible_close_date_bn(grievanceDTO.getExpectedDateOfClosingBangla())
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

}
