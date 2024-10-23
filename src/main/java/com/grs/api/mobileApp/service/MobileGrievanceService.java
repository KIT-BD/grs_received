package com.grs.api.mobileApp.service;

import com.grs.api.model.request.FileDTO;
import com.grs.api.model.request.GrievanceWithoutLoginRequestDTO;
import com.grs.api.model.response.file.FileBaseDTO;
import com.grs.api.model.response.file.FileContainerDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.core.domain.ServiceType;
import com.grs.core.service.GrievanceService;
import com.grs.core.service.StorageService;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.WeakHashMap;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MobileGrievanceService {

    @Autowired
    private GrievanceService grievanceService;

    @Autowired
    private StorageService storageService;

    private final GrievanceRepo grievanceRepo;
    private final ComplainantService complainantService;
    private final CitizenCharterDAO citizenCharterDAO;

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
