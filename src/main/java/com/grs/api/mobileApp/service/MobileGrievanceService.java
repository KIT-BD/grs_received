package com.grs.api.mobileApp.service;

import com.grs.api.model.request.FileDTO;
import com.grs.api.model.request.GrievanceWithoutLoginRequestDTO;
import com.grs.api.model.response.file.FileBaseDTO;
import com.grs.api.model.response.file.FileContainerDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.core.service.GrievanceService;
import com.grs.core.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
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
            String email, String divisionId, String districtId,
            String upazilaId, String complaintCategory,
            String fileNameByUser, List<MultipartFile> files,
            Principal principal) throws Exception {

        // Create GrievanceWithoutLoginRequestDTO
        GrievanceWithoutLoginRequestDTO grievanceRequestDTO = new GrievanceWithoutLoginRequestDTO();
        grievanceRequestDTO.setOfficeId(officeId);
        grievanceRequestDTO.setBody(description);
        grievanceRequestDTO.setSubject(subject);
        grievanceRequestDTO.setSpProgrammeId(spProgrammeId);
        grievanceRequestDTO.setComplainantPhoneNumber(mobileNumber);
        grievanceRequestDTO.setName(name);
        grievanceRequestDTO.setEmail(email);
        grievanceRequestDTO.setDivision(divisionId);
        grievanceRequestDTO.setDistrict(districtId);
        grievanceRequestDTO.setUpazila(upazilaId);
        grievanceRequestDTO.setGrievanceCategory(Integer.parseInt(complaintCategory));
        grievanceRequestDTO.setIsAnonymous(true);

        // Handle file uploads
        if (files != null && !files.isEmpty()) {
            FileContainerDTO fileContainerDTO = storageService.storeFileNew(principal, files.toArray(new MultipartFile[0]));
            List<FileBaseDTO> fileBaseDTOList = fileContainerDTO.getFiles();
            List<FileDTO> fileDTOS = new ArrayList<>();
            for (FileBaseDTO f : fileBaseDTOList) {
                FileDerivedDTO fileDerivedDTO = (FileDerivedDTO) f;
                fileDTOS.add(
                        FileDTO.builder()
                                .name(fileDerivedDTO.getName())
                                .url(fileDerivedDTO.getUrl())
                                .build()
                );
            }
            grievanceRequestDTO.setFiles(fileDTOS);
        }

        return grievanceService.addGrievanceWithoutLogin(null, grievanceRequestDTO);
    }
}
