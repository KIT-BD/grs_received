package com.grs.api.mobileApp.controller;

import com.grs.api.mobileApp.service.MobileGrievanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.WeakHashMap;

@RestController
@RequestMapping("/api/public-grievance")
public class MobileGrievanceController {

    @Autowired
    private MobileGrievanceService mobileGrievanceService;

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WeakHashMap<String, Object>> savePublicGrievance(
            @RequestParam("officeId") String officeId,
            @RequestParam("description") String description,
            @RequestParam("subject") String subject,
            @RequestParam(value = "sp_programme_id", required = false) String spProgrammeId,
            @RequestParam("mobile_number") String mobileNumber,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "division_id", required = false) String divisionId,
            @RequestParam(value = "district_id", required = false) String districtId,
            @RequestParam(value = "upazila_id", required = false) String upazilaId,
            @RequestParam("complaint_category") String complaintCategory,
            @RequestParam(value = "fileNameByUser", required = false) String fileNameByUser,
            @RequestPart(value = "files[]", required = false) List<MultipartFile> files,
            Principal principal) throws Exception {

        // Call the service method
        WeakHashMap<String, Object> response = mobileGrievanceService.savePublicGrievanceService(
                officeId, description, subject, spProgrammeId, mobileNumber, name,
                email, divisionId, districtId, upazilaId, complaintCategory,
                fileNameByUser, files, principal
        );

        return ResponseEntity.ok(response);
    }
}