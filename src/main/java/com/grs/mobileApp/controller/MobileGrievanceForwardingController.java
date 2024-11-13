package com.grs.mobileApp.controller;

import com.grs.api.model.response.GenericResponse;
import com.grs.mobileApp.dto.MobileGrievanceForwardingRequest;
import com.grs.mobileApp.service.MobileGrievanceForwardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MobileGrievanceForwardingController {

    private final MobileGrievanceForwardingService mobileGrievanceForwardingService;

    @RequestMapping(value = "/api/administrative-grievance/send-for-opinion", method = RequestMethod.POST)
    public Map<String,Object> sendForOpinion(Authentication authentication, @RequestBody MobileGrievanceForwardingRequest grievanceOpinionRequestDTO) throws IOException {

        return mobileGrievanceForwardingService.sendForOpinion(authentication,grievanceOpinionRequestDTO);
    }

    @RequestMapping(value = "/api/administrative-grievance/send-to-another-office", method = RequestMethod.POST)
    public Map<String, Object> forwardToAnotherOffice(Authentication authentication, @RequestBody MobileGrievanceForwardingRequest mobileGrievanceForwardingRequest) {
        return mobileGrievanceForwardingService.forwardToAnotherOffice(authentication, mobileGrievanceForwardingRequest);
    }

    @RequestMapping(value = "/api/administrative-grievance/reject-grievance", method = RequestMethod.POST)
    public Map<String, Object> rejectGrievance(
            Authentication authentication,
            @RequestParam Long complaint_id,
            @RequestParam Long office_id,
            @RequestParam Long username,
            @RequestParam String note,
            @RequestParam String fileNameByUser,
            @RequestParam(value = "files[]", required = false) List<MultipartFile> files,
            Principal principal) {
        return mobileGrievanceForwardingService.rejectGrievance(authentication, complaint_id, office_id, username, note, fileNameByUser, files, principal);
    }
}
