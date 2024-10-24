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
    private final ComplainantService complainantService;

    @PostMapping(value = "/api/public-grievance/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MobileResponseNoList savePublicGrievance(
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
        MobileGrievanceResponseDTO response = mobileGrievanceService.savePublicGrievanceService(
                officeId, description, subject, spProgrammeId, mobileNumber, name,
                email, divisionId, districtId, upazilaId, complaintCategory,
                fileNameByUser, files, principal
        );

        return MobileResponseNoList.builder()
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
    public MobileResponseNoList submitMobileGrievanceWithLogin(
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
            return MobileResponseNoList.builder()
                    .status("error")
                    .data("Invalid token for current user")
                    .build();
        }
        Complainant complainant = complainantService.findOne(complainantId);

        MobileGrievanceResponseDTO responseDTO = mobileGrievanceService.saveGrievanceWithLogin(
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

        return MobileResponseNoList.builder()
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
    public MobileResponse getGrievanceDetails(
            @RequestParam("complaint_id") Long complaintId
    ) {
        List<MobileGrievanceResponseDTO> grievanceList = mobileGrievanceService.findGrievancesByUser(complaintId);

        if (grievanceList == null){
            return MobileResponse.builder()
                    .status("error")
                    .data(new ArrayList<>())
                    .build();
        }

        return MobileResponse.builder()
                .status("success")
                .data(grievanceList)
                .build();
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
        return MobileResponse.builder()
                .status("success")
                .data(grievanceList)
                .build();
    }
}
