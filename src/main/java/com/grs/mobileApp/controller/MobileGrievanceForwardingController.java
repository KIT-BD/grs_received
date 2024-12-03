package com.grs.mobileApp.controller;

import com.grs.api.model.request.FileDTO;
import com.grs.mobileApp.dto.*;
import com.grs.mobileApp.service.MobileGrievanceForwardingService;
import com.grs.utils.BanglaConverter;
import com.grs.utils.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MobileGrievanceForwardingController {

    private final MobileGrievanceForwardingService mobileGrievanceForwardingService;
    private final FileUploadUtil fileUploadUtil;

    @RequestMapping(value = "/api/administrative-grievance/send-for-opinion", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> sendForOpinion(
            @RequestParam Long complaint_id,
            @RequestParam Long office_id,
            @RequestParam String username,
            @RequestParam String note,
            @RequestParam String deadline,
            @RequestParam String officers,
            @RequestParam(value = "files[]", required = false) List<MultipartFile> files,
            @RequestParam(value = "fileNameByUser", required = false) String file_name_by_user,
            Authentication authentication,
            Principal principal
    ) throws ParseException {

        List<FileDTO> convertedFiles = null;
        if (files != null && !files.isEmpty()) {
            convertedFiles = fileUploadUtil.getFileDTOFromMultipart(files, file_name_by_user, principal);
        }

        String deadLineEn = BanglaConverter.convertToEnglish(deadline);

        MobileGrievanceForwardingRequest grievanceOpinionRequestDTO = MobileGrievanceForwardingRequest.builder()
                .complaint_id(complaint_id)
                .office_id(office_id)
                .username(username)
                .note(note)
                .deadline(deadLineEn)
                .officers(officers)
                .file_name_by_user(file_name_by_user)
                .files(convertedFiles)
                .build();

        return mobileGrievanceForwardingService.sendForOpinion(authentication, grievanceOpinionRequestDTO);
    }

    //================================================================================================================================================

    @RequestMapping(value = "/api/administrative-grievance/investigation", method = RequestMethod.POST)
    public Map<String, Object> takingInvestigationStep(
            Authentication authentication,
            @RequestParam Long complaint_id,
            @RequestParam Long office_id,
            @RequestParam String note,
            @RequestParam Long to_employee_record_id,
            @RequestParam String officers
    ) throws ParseException {

        MobileInvestigationForwardingDTO mobileInvestigationForwardingDTO = MobileInvestigationForwardingDTO.builder()
                .Complaint_id(complaint_id)
                .office_id(office_id)
                .note(note)
                .to_employee_record_id(to_employee_record_id)
                .officers(officers)
                .build();

        return mobileGrievanceForwardingService.sendForInvestigation(authentication, mobileInvestigationForwardingDTO);

    }


    @RequestMapping(value = "/api/administrative-grievance/give-opinion", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> giveOpinion(

            @RequestParam Long complaint_id,
            @RequestParam Long office_id,
            @RequestParam String note,
            @RequestPart(value = "files[]", required = false) List<MultipartFile> files,
            @RequestParam(value = "fileNameByUser", required = false) String fileNameByUser,
            Authentication authentication,
            Principal principal
    ) throws ParseException {


        List<FileDTO> convertedFiles = null;
        if (files != null && !files.isEmpty()) {
            convertedFiles = fileUploadUtil.getFileDTOFromMultipart(files, fileNameByUser, principal);
        }


        MobileOpinionForwardingDTO mobileOpinionForWardingDTO = MobileOpinionForwardingDTO.builder()
                .complaint_id(complaint_id)
                .office_id(office_id)
                .note(note)
                .files(convertedFiles)
                .fileNameByUser(fileNameByUser)
                .build();

        return mobileGrievanceForwardingService.giveOpinion(mobileOpinionForWardingDTO, authentication);

    }


    @RequestMapping(value = "/api/administrative-grievance/close-grievance", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> closeGrievance(
            @RequestParam Long complaint_id,
            @RequestParam Long office_id,
            @RequestParam String action,
            @RequestParam String closingNoteGRODecision,
            @RequestParam String closingNoteMainReason,
            @RequestParam String closingNoteSuggestion,
            @RequestParam (required = false) List<Long> deptAction,
            @RequestParam (required = false) String departmentalActionReason,
            @RequestParam (value = "files[]",required = false)List<MultipartFile> files,
            @RequestParam (value = "fileNameByUser",required = false)String fileNameByUser,
            Authentication authentication,
            Principal principal
    ) throws ParseException {

        List<FileDTO> convertedFiles = null;
        if (files != null && !files.isEmpty()) {
            convertedFiles = fileUploadUtil.getFileDTOFromMultipart(files, fileNameByUser, principal);
        }

        MobileGrievanceCloseForwardingDTO mobileGrievanceCloseForwardingDTO = MobileGrievanceCloseForwardingDTO.builder()
                .complaint_id(complaint_id)
                .office_id(office_id)
                .departmentalActionReason(departmentalActionReason)
                .closingNoteGRODecision(closingNoteGRODecision)
                .closingNoteSuggestion(closingNoteSuggestion)
                .closingNoteMainReason(closingNoteMainReason)
                .action(action)
                .deptAction(deptAction)
                .files(convertedFiles)
                .build();

        return mobileGrievanceForwardingService.closeGievance(mobileGrievanceCloseForwardingDTO,authentication);

    }

    @RequestMapping(value = "/api/administrative-grievance/provide-investigation-report", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,Object> provideInvestigationReport(
            Authentication authentication,
            @RequestParam Long complaint_id,
            @RequestParam String note,
            @RequestParam(value = "files[]") List<MultipartFile> files,
            @RequestParam(value = "fileNameByUser") String fileNameByUser,
            Principal principal

    ) throws ParseException {
        List<FileDTO> convertedFiles = null;
        if (files != null && !files.isEmpty()) {
            convertedFiles = fileUploadUtil.getFileDTOFromMultipart(files, fileNameByUser, principal);
        }

        MobileInvestigationReportForwardingDTO mobileInvestigationReportForwardingDTO = MobileInvestigationReportForwardingDTO.builder()
                .complaint_id(complaint_id)
                .file(convertedFiles)
                .note(note)
                .build();

        return mobileGrievanceForwardingService.provideInvestigationReport(mobileInvestigationReportForwardingDTO,authentication);


    }

    //================================================================================================================================================


    @RequestMapping(value = "/api/administrative-grievance/send-to-another-office", method = RequestMethod.POST)
    public Map<String, Object> forwardToAnotherOffice(
            Authentication authentication,
            @RequestParam Long complaint_id,
            @RequestParam Long office_id,
            @RequestParam String note,
            @RequestParam(value = "other_service", required = false) String other_service,
            @RequestParam(value = "service_id", required = false) Long service_id,
            @RequestParam String username,
            @RequestPart(value = "files[]", required = false) List<MultipartFile> files,
            @RequestParam(value = "fileNameByUser", required = false) String fileNameByUser,
            Principal principal) throws ParseException {

        return mobileGrievanceForwardingService.forwardToAnotherOffice(authentication, complaint_id, office_id, note, other_service, service_id, username, files, fileNameByUser, principal);
    }

    @RequestMapping(value = "/api/administrative-grievance/reject-grievance", method = RequestMethod.POST)
    public Map<String, Object> rejectGrievance(
            Authentication authentication,
            @RequestParam Long complaint_id,
            @RequestParam Long office_id,
            @RequestParam String username,
            @RequestParam String note,
            @RequestParam(value = "fileNameByUser", required = false) String fileNameByUser,
            @RequestParam(value = "files[]", required = false) List<MultipartFile> files,
            Principal principal) throws ParseException {
        return mobileGrievanceForwardingService.rejectGrievance(authentication, complaint_id, office_id, username, note, fileNameByUser, files, principal);
    }

    @RequestMapping(value = "/api/administrative-grievance/send-to-subordinate-office", method = RequestMethod.POST)
    public Map<String, Object> sendToAppealOfficerOrSubordinateOffice(
            Authentication authentication,
            @RequestParam(value = "complaint_id") Long complaint_id,
            @RequestParam(value = "note") String note,
            @RequestParam(value = "office_id", required = false) Long office_id,
            @RequestParam(value = "other_service", required = false) String other_service,
            @RequestParam(value = "service_id", required = false) Long service_id,
            @RequestParam(value = "files[]", required = false) List<MultipartFile> files,
            @RequestParam(value = "fileNameByUser", required = false) String fileNameByUser
    ) throws ParseException {
        return mobileGrievanceForwardingService.sendToAppealOfficerOrSubordinateOffice(
                authentication,
                complaint_id,
                note,
                office_id,
                other_service,
                service_id,
                files,
                fileNameByUser);
    }

    @RequestMapping(value = "/api/administrative-grievance/request-document", method = RequestMethod.POST)
    public Map<String,Object> documentRequest(
            Authentication authentication,
            @RequestParam Long complaint_id,
            @RequestParam(required = false) Long office_id,
            @RequestParam(required = false) Long to_employee_record_id,
            @RequestParam String note
            ) throws ParseException {
        return mobileGrievanceForwardingService.requestDocument(authentication, complaint_id, note);
    }

    @RequestMapping(value = "/api/administrative-grievance/give-guidelines-to-providing-services", method = RequestMethod.POST)
    public Map<String, Object> giveGuidelinesToProvidingServices(
            Authentication authentication,
            @RequestParam(value = "complaint_id") Long complaint_id,
            @RequestParam(value = "office_id", required = false) Long office_id,
            @RequestParam(value = "note") String note,
            @RequestParam(value = "deadline") String deadline,
            @RequestParam(value = "guidance_receiver") String guidance_receiver

    ) throws ParseException, IOException {
        return mobileGrievanceForwardingService.giveGuidelinesToProvidingServices(
                authentication,
                complaint_id,
                office_id,
                note,
                deadline,
                guidance_receiver);
    }
    @RequestMapping(value = "/api/administrative-grievance/ask-for-permission", method = RequestMethod.POST)
        public Map<String, Object> askForPermission(
                Authentication authentication,
                @RequestParam(value = "complaint_id") Long complaint_id,
                @RequestParam(value = "note") String note

        ) throws ParseException {
            return mobileGrievanceForwardingService.askForPermission(
                    authentication,
                    complaint_id,
                    note);
        }

    @RequestMapping(value = "/api/administrative-grievance/hearing-notice", method = RequestMethod.POST)
    public Map<String, Object> hearingNotice(
            Authentication authentication,
            @RequestParam Long complaint_id,
            @RequestParam String hearing_date,
            @RequestParam String hearing_time,
            @RequestParam String note
    ) throws ParseException {
        return mobileGrievanceForwardingService.hearingNotice(authentication, complaint_id, hearing_date, hearing_time, note);
    }
}
