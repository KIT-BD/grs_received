package com.grs.mobileApp.controller;

import com.grs.api.model.request.FileDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.mobileApp.dto.MobileGrievanceForwardingRequest;
import com.grs.mobileApp.service.MobileGrievanceForwardingService;
import com.grs.utils.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MobileGrievanceForwardingController {

    private final MobileGrievanceForwardingService mobileGrievanceForwardingService;
    private final FileUploadUtil fileUploadUtil;

    @RequestMapping(value = "/api/administrative-grievance/send-for-opinion", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,Object> sendForOpinion(
            @RequestParam Long complaint_id,
            @RequestParam Long office_id,
            @RequestParam String username,
            @RequestParam String note,
            @RequestParam String deadline,
            @RequestParam String officers,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("fileNameByUser") String file_name_by_user,
            Authentication authentication,
            Principal principal
    ){

        MobileGrievanceForwardingRequest grievanceOpinionRequestDTO = MobileGrievanceForwardingRequest.builder()
                .complaint_id(complaint_id)
                .office_id(office_id)
                .username(username)
                .note(note)
                .deadline(deadline)
                .officers(officers)
                .file_name_by_user(file_name_by_user)
                .files(fileUploadUtil.getFileDTOFromMultipart(files, file_name_by_user, principal))
                .build();

        Map<String,Object> opinionResponse = mobileGrievanceForwardingService.sendForOpinion(authentication,grievanceOpinionRequestDTO);

        Map<String,Object> response = new HashMap<>();
        response.put("data",grievanceOpinionRequestDTO);
        return response;

//        return mobileGrievanceForwardingService.sendForOpinion(authentication,grievanceOpinionRequestDTO);
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
