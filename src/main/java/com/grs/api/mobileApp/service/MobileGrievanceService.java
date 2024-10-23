package com.grs.api.mobileApp.service;

import com.grs.api.mobileApp.dto.MobileGrievanceResponseDTO;
import com.grs.api.model.request.FileDTO;
import com.grs.api.model.request.GrievanceWithoutLoginRequestDTO;
import com.grs.api.model.response.file.FileBaseDTO;
import com.grs.api.model.response.file.FileContainerDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.core.dao.GrievanceDAO;
import com.grs.core.domain.ServiceType;
import com.grs.core.domain.grs.ServiceOrigin;
import com.grs.core.service.GrievanceService;
import com.grs.core.service.StorageService;
import com.grs.utils.BanglaConverter;
import org.springframework.beans.factory.annotation.Autowired;
import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.api.model.request.GrievanceRequestDTO;
import com.grs.core.dao.CitizenCharterDAO;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.MediumOfSubmission;
import com.grs.core.domain.grs.CitizenCharter;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.Grievance;
import com.grs.core.repo.grs.GrievanceRepo;
import com.grs.core.service.ComplainantService;
import com.grs.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                //.OfficeLayers("HQ>Division>District")
                .serviceId(serviceId == null ? String.valueOf(ServiceType.NAGORIK.ordinal()) : serviceId)
                .submissionDate(String.valueOf(new Date()))
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

    public WeakHashMap<String, Object> savePublicGrievanceService(
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
        grievanceDTO.setOfficeLayers(null);
        grievanceDTO.setServiceId("0");
        grievanceDTO.setSubmissionDate(String.valueOf(new Date()));
        grievanceDTO.setSubject(subject);
        grievanceDTO.setBody(description);
        grievanceDTO.setRelation(null);
        grievanceDTO.setServiceReceiver(null);
        grievanceDTO.setServiceOthers(null);
        grievanceDTO.setIsAnonymous(false);
        grievanceDTO.setServiceType(ServiceType.NAGORIK);
        //grievanceDTO.setFiles(new ArrayList<>()); // assuming FileDTO objects will be added later
        grievanceDTO.setOfflineGrievanceUpload(false);
        grievanceDTO.setPhoneNumber(null);
        grievanceDTO.setIsSelfMotivated(true);
        grievanceDTO.setSourceOfGrievance("Mobile App");
        grievanceDTO.setUser(null);
        grievanceDTO.setSecret(null);
        grievanceDTO.setSubmittedThroughApi(0);
        grievanceDTO.setGrievanceCategory(complaintCategory);
        grievanceDTO.setSpProgrammeId(spProgrammeId);
        grievanceDTO.setDivision(null);
        grievanceDTO.setDistrict(null);
        grievanceDTO.setUpazila(null);
        grievanceDTO.setSafetyNetId(0);
        grievanceDTO.setDivisionId(divisionId);
        grievanceDTO.setDistrictId(districtId);
        grievanceDTO.setUpazilaId(upazilaId);

//        // Create GrievanceWithoutLoginRequestDTO
//        GrievanceWithoutLoginRequestDTO grievanceDTO = new GrievanceWithoutLoginRequestDTO();
//        grievanceDTO.setOfficeId(officeId);
//        grievanceDTO.setBody(description);
//        grievanceDTO.setSubject(subject);
//        grievanceDTO.setSpProgrammeId(spProgrammeId);
//        grievanceDTO.setComplainantPhoneNumber(mobileNumber);
//        grievanceDTO.setName(name);
//        grievanceDTO.setEmail(email);
//        grievanceDTO.setDivision(divisionId);
//        grievanceDTO.setDistrict(districtId);
//        grievanceDTO.setUpazila(upazilaId);
//        grievanceDTO.setGrievanceCategory(Integer.parseInt(complaintCategory));
//        grievanceDTO.setIsAnonymous(true);

        // Handle file uploads
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
                System.out.println("Name: " + fileNames[i-1]);
                System.out.println("URL: " + fileDerivedDTO.getUrl());
            }
            grievanceDTO.setFiles(fileDTOS);
        }

        return grievanceService.addGrievanceWithoutLogin(null, grievanceDTO);
    }
    public List<Grievance> findGrievancesByUser(Long id){
        return grievanceRepo.findGrievancesByComplainantId(id);
    }

//    public Grievance submitGrievance(GrievanceRequestDTO grievanceRequestDTO){
//        return submitGrievance(null, grievanceRequestDTO);
//    }

//    public Grievance submitGrievance(UserInformation userInformation, GrievanceRequestDTO grievanceRequestDTO) {
//        boolean isGrsUser = false;
//        boolean offlineGrievanceUploaded = false;
//        Long uploaderGroOfficeUnitOrganogramId = null;
//        Long userId;
//
//        if (userInformation == null) {
//            userId = 0L;
//            isGrsUser = true;
//        } else {
//            isGrsUser = userInformation.getUserType().equals(UserType.COMPLAINANT);
//            userId = isGrsUser ? userInformation.getUserId() : userInformation.getOfficeInformation().getEmployeeRecordId();
//        }
//
//        boolean isAnonymous = (grievanceRequestDTO.getIsAnonymous() != null && grievanceRequestDTO.getIsAnonymous());
//        if (grievanceRequestDTO.getOfflineGrievanceUpload() != null && grievanceRequestDTO.getOfflineGrievanceUpload()) {
//            offlineGrievanceUploaded = true;
//            uploaderGroOfficeUnitOrganogramId = userInformation != null && userInformation.getOfficeInformation() != null ? userInformation.getOfficeInformation().getOfficeUnitOrganogramId() : null;
//            if (!isAnonymous) {
//                Complainant complainant = this.complainantService.findComplainantByPhoneNumber(grievanceRequestDTO.getPhoneNumber());
//                userId = complainant.getId();
//            }
//            isGrsUser = true;
//        }
//
//        Long officeId = Long.valueOf(grievanceRequestDTO.getOfficeId());
//        CitizenCharter citizenCharter = null;
//
//        // Check for null or empty serviceId before parsing
//        if (grievanceRequestDTO.getServiceId() != null && !grievanceRequestDTO.getServiceId().isEmpty()) {
//            citizenCharter = this.citizenCharterDAO.findByOfficeIdAndServiceId(officeId, Long.valueOf(grievanceRequestDTO.getServiceId()));
//        }
//
//        GrievanceCurrentStatus currentStatus = (officeId == 0 ? GrievanceCurrentStatus.CELL_NEW : GrievanceCurrentStatus.NEW);
//        MediumOfSubmission medium = MediumOfSubmission.ONLINE;
//
//        if (currentStatus.equals(GrievanceCurrentStatus.NEW) && grievanceRequestDTO.getOfflineGrievanceUpload() != null && grievanceRequestDTO.getOfflineGrievanceUpload()) {
//            medium = MediumOfSubmission.CONVENTIONAL_METHOD;
//        } else if (currentStatus.equals(GrievanceCurrentStatus.NEW) && grievanceRequestDTO.getIsSelfMotivated() != null && grievanceRequestDTO.getIsSelfMotivated()) {
//            medium = MediumOfSubmission.SELF_MOTIVATED_ACCEPTANCE;
//        }
//
//        Grievance grievance = Grievance.builder()
//                .details(grievanceRequestDTO.getBody())
//                .subject(grievanceRequestDTO.getSubject())
//                .officeId(officeId)
//                .officeLayers(grievanceRequestDTO.getOfficeLayers())
//                .serviceOrigin(citizenCharter == null ? null : citizenCharter.getServiceOrigin())
//                .serviceOriginBeforeForward(citizenCharter == null ? null : citizenCharter.getServiceOrigin())
//                .grsUser(isGrsUser)
//                .complainantId(isAnonymous ? 0L : userId)
//                .grievanceType(grievanceRequestDTO.getServiceType())
//                .grievanceCurrentStatus(currentStatus)
//                .isAnonymous(isAnonymous)
//                .trackingNumber(StringUtil.isValidString(grievanceRequestDTO.getServiceTrackingNumber()) ? grievanceRequestDTO.getServiceTrackingNumber() : null)
//                .otherService(grievanceRequestDTO.getServiceOthers())
//                .otherServiceBeforeForward(grievanceRequestDTO.getServiceOthers())
//                .serviceReceiver(grievanceRequestDTO.getServiceReceiver())
//                .serviceReceiverRelation(grievanceRequestDTO.getRelation())
//                .isOfflineGrievance(offlineGrievanceUploaded)
//                .uploaderOfficeUnitOrganogramId(uploaderGroOfficeUnitOrganogramId)
//                .isSelfMotivatedGrievance(grievanceRequestDTO.getIsSelfMotivated() != null && grievanceRequestDTO.getIsSelfMotivated())
//                .sourceOfGrievance(grievanceRequestDTO.getSourceOfGrievance())
//                .complaintCategory(grievanceRequestDTO.getGrievanceCategory())
//                .mediumOfSubmission(medium.name())
//                .spProgrammeId(StringUtil.isValidString(grievanceRequestDTO.getSpProgrammeId()) ? Integer.parseInt(grievanceRequestDTO.getSpProgrammeId()) : null)
//                .geoDivisionId(StringUtil.isValidString(grievanceRequestDTO.getDivision()) ? Integer.parseInt(grievanceRequestDTO.getDivision()) : null)
//                .geoDistrictId(StringUtil.isValidString(grievanceRequestDTO.getDistrict()) ? Integer.parseInt(grievanceRequestDTO.getDistrict()) : null)
//                .geoUpazilaId(StringUtil.isValidString(grievanceRequestDTO.getUpazila()) ? Integer.parseInt(grievanceRequestDTO.getUpazila()) : null)
//                .build();
//
//        grievance.setStatus(true);
//        grievance = grievanceRepo.save(grievance);
//        return grievance;
//    }
}
