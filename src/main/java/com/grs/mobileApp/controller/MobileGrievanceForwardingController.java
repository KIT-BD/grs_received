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

//        Map<String,Object> response = new HashMap<>();
//        response.put("data",grievanceOpinionRequestDTO);
        return mobileGrievanceForwardingService.sendForOpinion(authentication,grievanceOpinionRequestDTO);

//        return mobileGrievanceForwardingService.sendForOpinion(authentication,grievanceOpinionRequestDTO);
    }

    @RequestMapping(value = "/api/administrative-grievance/send-to-another-office", method = RequestMethod.POST)
    public Map<String, Object> forwardToAnotherOffice(
            Authentication authentication,
            @RequestParam Long complaint_id,
            @RequestParam Long office_id,
            @RequestParam String note,
            @RequestParam(value = "other_service", required = false) String other_service,
            @RequestParam(value = "service_id", required = false) Long service_id,
            @RequestParam String username,
            @RequestParam(value = "files[]", required = false) List<MultipartFile> files,
            @RequestParam(value = "fileNameByUser", required = false) String fileNameByUser,
            Principal principal) {

        return mobileGrievanceForwardingService.forwardToAnotherOffice(authentication, complaint_id, office_id, note, other_service, service_id, username, files, fileNameByUser, principal);
    }

    @RequestMapping(value = "/api/administrative-grievance/reject-grievance", method = RequestMethod.POST)
    public Map<String, Object> rejectGrievance(
            Authentication authentication,
            @RequestParam Long complaint_id,
            @RequestParam Long office_id,
            @RequestParam Long username,
            @RequestParam String note,
            @RequestParam(value = "fileNameByUser", required = false) String fileNameByUser,
            @RequestParam(value = "files[]", required = false) List<MultipartFile> files,
            Principal principal) {
        return mobileGrievanceForwardingService.rejectGrievance(authentication, complaint_id, office_id, username, note, fileNameByUser, files, principal);
    }
}
