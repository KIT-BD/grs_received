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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.WeakHashMap;

@Service
public class MobileGrievanceService {

    @Autowired
    private GrievanceService grievanceService;

    @Autowired
    private StorageService storageService;

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
}
