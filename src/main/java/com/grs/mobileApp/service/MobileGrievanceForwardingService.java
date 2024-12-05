package com.grs.mobileApp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grs.api.model.UserInformation;
import com.grs.api.model.request.*;
import com.grs.api.model.response.GenericResponse;
import com.grs.api.model.response.grievanceForwarding.GrievanceForwardingInvestigationDTO;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.ServiceType;
import com.grs.core.repo.projapoti.OfficeRepo;
import com.grs.core.service.GrievanceForwardingService;
import com.grs.mobileApp.dto.MobileGrievanceForwardingRequest;
import com.grs.mobileApp.dto.MobileGrievanceResponseDTO;
import com.grs.mobileApp.dto.MobileOfficerDTO;
import com.grs.core.domain.projapoti.Office;
import com.grs.mobileApp.dto.*;
import com.grs.utils.BanglaConverter;
import com.grs.utils.FileUploadUtil;
import com.grs.utils.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.security.Principal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.grs.utils.BanglaConverter.*;

@Service
public class MobileGrievanceForwardingService {

    @Autowired
    private GrievanceForwardingService grievanceForwardingService;

    @Autowired
    private OfficeRepo officeRepo;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Autowired
    private MobileGrievanceService mobileGrievanceService;


    public Map<String, Object> sendForOpinion(
            Authentication authentication,
            MobileGrievanceForwardingRequest mobileGrievanceForwardingRequest) throws ParseException {

        ObjectMapper objectMapper = new ObjectMapper();
        List<MobileOfficerDTO> officerDTOList = null;
        try {
            officerDTOList = objectMapper.readValue(mobileGrievanceForwardingRequest.getOfficers(), new TypeReference<List<MobileOfficerDTO>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert officerDTOList != null;
        Optional<MobileOfficerDTO> primary = officerDTOList.stream().filter(officer -> officer.getReceiverCheck() && !officer.getCcCheck()).findFirst();

        List<MobileOfficerDTO> ccList = officerDTOList.stream()
                .filter(officer -> !officer.getReceiverCheck() && officer.getCcCheck())
                .collect(Collectors.toList());


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(mobileGrievanceForwardingRequest.getDeadline(), formatter);
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        assert primary.isPresent();
        Long postNodeMinistryId = officeRepo.findOfficeById(primary.get().getOffice_id()).getOfficeMinistry().getId();

        List<String> postNodeList = new ArrayList<>();
        postNodeList.add("post_" + postNodeMinistryId + "_" + primary.get().getOffice_id() + "_" + primary.get().getOffice_unit_organogram_id());

        List<String> ccNodeList = new ArrayList<>();
        for (MobileOfficerDTO m : ccList) {
            Long ccMinistry = officeRepo.findOfficeById(m.getOffice_id()).getOfficeMinistry().getId();
            ccNodeList.add("post_" + ccMinistry + "_" + m.getOffice_id() + "_" + m.getOffice_unit_organogram_id());
        }

        OpinionRequestDTO ReqToOp = OpinionRequestDTO.builder()
                .grievanceId(mobileGrievanceForwardingRequest.getComplaint_id())
                .comment(mobileGrievanceForwardingRequest.getNote())
                .files(mobileGrievanceForwardingRequest.getFiles())
                .postNode(postNodeList)
                .ccNode(ccNodeList)
                .deadline(date)
                .referredFiles(null)
                .build();

        Map<String, Object> errorMsg = new HashMap<>();
        if (!(ReqToOp.getPostNode() != null
                && ReqToOp.getPostNode().size() == 1
                && ReqToOp.getPostNode().get(0) != null)) {
            errorMsg.put("status", "error");
            errorMsg.put("message", "অনুগ্রহ করে মতামতের জন্য অন্ততপক্ষে যে কোন একজনকে নির্বাচন করুন");
            return errorMsg;
        }

        if (ReqToOp.getCcNode() != null) {
            for (String ccNode : ReqToOp.getCcNode()) {
                if (ccNode.equals(ReqToOp.getPostNode().get(0))) {
                    errorMsg.put("status", "error");
                    errorMsg.put("message", "অনুগ্রহ করে প্রধান প্রাপক ব্যতীত অন্য একজনকে অনুলিপি প্রাপক হিসেবে নির্বাচন করুন");
                    return errorMsg;
                }
            }
        }


        GenericResponse genericResponse = grievanceForwardingService.sendForOpinion(authentication, ReqToOp);
        Map<String, Object> response = new HashMap<>();

        if (genericResponse.isSuccess()) {

            Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(mobileGrievanceForwardingRequest.getComplaint_id());
            Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
            Object allComplaintDetails = data.get("allComplaintDetails");

            response.put("data", allComplaintDetails);
            response.put("status", "success");
            response.put("message", "The grievance has been sent for opinion successfully.");
            return response;
        } else {
            response.put("status", "error");
            response.put("message", "Error while sending for opinion.");
            response.put("data", null);
            return response;
        }
    }


    //================================================================================================================================================

    public Map<String, Object> sendForInvestigation(
            Authentication authentication,
            MobileInvestigationForwardingDTO mobileInvestigationForwardingDTO) throws ParseException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<MobileOfficerInvstDTO> officerDTOList = null;
        try {
            officerDTOList = objectMapper.readValue(mobileInvestigationForwardingDTO.getOfficers(), new TypeReference<List<MobileOfficerInvstDTO>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert officerDTOList != null;
        Optional<MobileOfficerInvstDTO> primary = officerDTOList.stream().filter(officer -> officer.isCommitteeHead()).findFirst();

        List<MobileOfficerInvstDTO> comitteeList = officerDTOList.stream()
                .filter(officer -> !officer.isCommitteeHead())
                .collect(Collectors.toList());


        assert primary.isPresent();
        Long postNodeMinistryId = officeRepo.findOfficeById(primary.get().getOffice_id()).getOfficeMinistry().getId();


        String head = "post_" + postNodeMinistryId + "_" + primary.get().getOffice_id() + "_" + primary.get().getOffice_unit_organogram_id();

        List<String> committee = new ArrayList<>();
        for (MobileOfficerInvstDTO m : comitteeList) {
            Long ccMinistry = officeRepo.findOfficeById(m.getOffice_id()).getOfficeMinistry().getId();
            committee.add("post_" + ccMinistry + "_" + m.getOffice_id() + "_" + m.getOffice_unit_organogram_id());
        }

        GrievanceForwardingInvestigationDTO grievanceForwardingInvestigationDTO = GrievanceForwardingInvestigationDTO.builder()
                .grievanceId(mobileInvestigationForwardingDTO.getComplaint_id())
                .note(mobileInvestigationForwardingDTO.getNote())
                .head(head)
                .committee(committee)
                .currentStatus(mobileInvestigationForwardingDTO.getCurrentStatus())
                .build();


        GenericResponse genericResponse = grievanceForwardingService.initiateInvestigation(grievanceForwardingInvestigationDTO, authentication);

        Map<String, Object> response = new HashMap<>();

        if (genericResponse.isSuccess()) {

            Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(mobileInvestigationForwardingDTO.getComplaint_id());
            Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
            Object allComplaintDetails = data.get("allComplaintDetails");

            response.put("data", allComplaintDetails);
            response.put("status", "success");
            response.put("message", "The grievance has been sent for investigation successfully.");
            return response;
        } else {
            response.put("status", "error");
            response.put("message", "Error while sending for opinion.");
            response.put("data", null);
            return response;
        }
    }


    public Map<String, Object> giveOpinion(MobileOpinionForwardingDTO mobileOpinionForWardingDTO,
                                           Authentication authentication) throws ParseException {

        OpinionRequestDTO opinionRequestDTO = OpinionRequestDTO.builder()
                .grievanceId(mobileOpinionForWardingDTO.getComplaint_id())
                .comment(mobileOpinionForWardingDTO.getNote())
                .files(mobileOpinionForWardingDTO.getFiles())
                .referredFiles(null)
                .build();


        GenericResponse genericResponse = grievanceForwardingService.giveOpinion(authentication, opinionRequestDTO);

        Map<String, Object> response = new HashMap<>();

        if (genericResponse.isSuccess()) {

            Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(mobileOpinionForWardingDTO.getComplaint_id());
            Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
            Object allComplaintDetails = data.get("allComplaintDetails");

            response.put("data", allComplaintDetails);
            response.put("status", "success");
            response.put("message", "The grievance has been sent for giving opinion successfully.");
            return response;
        } else {
            response.put("status", "error");
            response.put("message", "Error while sending for opinion.");
            response.put("data", null);
            return response;
        }

    }


    public Map<String, Object> closeGievance(MobileGrievanceCloseForwardingDTO mobileGrievanceCloseForwardingDTO, Authentication authentication) throws ParseException {

        if (GrievanceCurrentStatus.valueOf(mobileGrievanceCloseForwardingDTO.getAction()) == GrievanceCurrentStatus.CLOSED_ACCUSATION_PROVED) {

            if (!mobileGrievanceCloseForwardingDTO.getDeptAction().isEmpty()) {
                List<String> employeeList = new ArrayList<>();

                for (Map<String, Long> i : mobileGrievanceCloseForwardingDTO.getDeptAction()) {
                    employeeList.add(i.get("employeeRecordId") + "_" + i.get("officeUnitOrganogramId") + "_" + mobileGrievanceCloseForwardingDTO.getOffice_id());
                }

                GrievanceForwardingCloseDTO grievanceForwardingCloseDTO = GrievanceForwardingCloseDTO.builder()
                        .departmentalActionNote(mobileGrievanceCloseForwardingDTO.getDepartmentalActionReason())
                        .groDecision(mobileGrievanceCloseForwardingDTO.getClosingNoteGRODecision())
                        .mainReason(mobileGrievanceCloseForwardingDTO.getClosingNoteMainReason())
                        .groSuggestion(mobileGrievanceCloseForwardingDTO.getClosingNoteSuggestion())
                        .files(mobileGrievanceCloseForwardingDTO.getFiles())
                        .status(GrievanceCurrentStatus.valueOf(mobileGrievanceCloseForwardingDTO.getAction()))
                        .takeAction(true)
                        .employeeList(employeeList)
                        .referredFiles(null)
                        .build();

                GenericResponse genericResponse = grievanceForwardingService.closeGrievance(authentication, mobileGrievanceCloseForwardingDTO.getComplaint_id(), grievanceForwardingCloseDTO);
                Map<String, Object> response = new LinkedHashMap<>();
                if (genericResponse.isSuccess()) {

                    Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(mobileGrievanceCloseForwardingDTO.getComplaint_id());
                    Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
                    Object allComplaintDetails = data.get("allComplaintDetails");

                    response.put("data", allComplaintDetails);
                    response.put("status", "success");
                    response.put("message", "The grievance has been sent for giving opinion successfully.");
                    return response;
                } else {
                    response.put("status", "error");
                    response.put("message", "Error while sending for opinion.");
                    response.put("data", null);
                    return response;
                }
            } else {
                GrievanceForwardingCloseDTO grievanceForwardingCloseDTO = GrievanceForwardingCloseDTO.builder()
                        .groDecision(mobileGrievanceCloseForwardingDTO.getClosingNoteGRODecision())
                        .mainReason(mobileGrievanceCloseForwardingDTO.getClosingNoteMainReason())
                        .groSuggestion(mobileGrievanceCloseForwardingDTO.getClosingNoteSuggestion())
                        .files(mobileGrievanceCloseForwardingDTO.getFiles())
                        .status(GrievanceCurrentStatus.valueOf(mobileGrievanceCloseForwardingDTO.getAction()))
                        .takeAction(false)
                        .referredFiles(null)
                        .build();

                GenericResponse genericResponse = grievanceForwardingService.closeGrievance(authentication, mobileGrievanceCloseForwardingDTO.getComplaint_id(), grievanceForwardingCloseDTO);
                Map<String, Object> response = new LinkedHashMap<>();
                if (genericResponse.isSuccess()) {

                    Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(mobileGrievanceCloseForwardingDTO.getComplaint_id());
                    Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
                    Object allComplaintDetails = data.get("allComplaintDetails");

                    response.put("data", allComplaintDetails);
                    response.put("status", "success");
                    response.put("message", "The grievance has been sent for giving opinion successfully.");
                    return response;
                } else {
                    response.put("status", "error");
                    response.put("message", "Error while sending for opinion.");
                    response.put("data", null);
                    return response;
                }
            }


        } else {
            GrievanceForwardingCloseDTO grievanceForwardingCloseDTO = GrievanceForwardingCloseDTO.builder()
                    .groDecision(mobileGrievanceCloseForwardingDTO.getClosingNoteGRODecision())
                    .mainReason(mobileGrievanceCloseForwardingDTO.getClosingNoteMainReason())
                    .groSuggestion(mobileGrievanceCloseForwardingDTO.getClosingNoteSuggestion())
                    .files(mobileGrievanceCloseForwardingDTO.getFiles())
                    .status(GrievanceCurrentStatus.valueOf(mobileGrievanceCloseForwardingDTO.getAction()))
                    .takeAction(false)
                    .referredFiles(null)
                    .build();

            GenericResponse genericResponse = grievanceForwardingService.closeGrievance(authentication, mobileGrievanceCloseForwardingDTO.getComplaint_id(), grievanceForwardingCloseDTO);
            Map<String, Object> response = new LinkedHashMap<>();
            if (genericResponse.isSuccess()) {

                Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(mobileGrievanceCloseForwardingDTO.getComplaint_id());
                Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
                Object allComplaintDetails = data.get("allComplaintDetails");

                response.put("data", allComplaintDetails);
                response.put("status", "success");
                response.put("message", "The grievance has been sent for giving opinion successfully.");
                return response;
            } else {
                response.put("status", "error");
                response.put("message", "Error while sending for opinion.");
                response.put("data", null);
                return response;
            }
        }


    }
    public Map<String, Object> provideInvestigationReport(MobileInvestigationReportForwardingDTO mobileInvestigationReportForwardingDTO, Authentication authentication) throws ParseException {

        GrievanceForwardingNoteDTO grievanceForwardingNoteDTO = GrievanceForwardingNoteDTO.builder()
                .grievanceId(mobileInvestigationReportForwardingDTO.getComplaint_id())
                .note(mobileInvestigationReportForwardingDTO.getNote())
                .files(mobileInvestigationReportForwardingDTO.getFile())
                .referredFiles(new ArrayList<>())
                .build();


        GenericResponse genericResponse = grievanceForwardingService.investigationReportSubmission(grievanceForwardingNoteDTO, authentication);
        Map<String, Object> response = new HashMap<>();

        if (genericResponse.isSuccess()) {

            Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(mobileInvestigationReportForwardingDTO.getComplaint_id());
            Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
            Object allComplaintDetails = data.get("allComplaintDetails");

            response.put("data", allComplaintDetails);
            response.put("status", "success");
            response.put("message", "The investigation report provided successfully.");
            return response;
        } else {
            response.put("status", "error");
            response.put("message", "investigation report forwarding error while forwarding to another office.");
            return response;
        }


    }


    public Map<String, Object> hearingTaking(MobileTakeHearingForwardingDTO mobileTakeHearingForwardingDTO, Authentication authentication) throws ParseException {


        GrievanceForwardingNoteDTO grievanceForwardingNoteDTO = GrievanceForwardingNoteDTO.builder()
                .grievanceId(mobileTakeHearingForwardingDTO.getGrievanceId())
                .note(mobileTakeHearingForwardingDTO.getNote())
                .files(mobileTakeHearingForwardingDTO.getFiles())
                .referredFiles(null)
                .currentStatus(null)
                .build();

        GenericResponse genericResponse = grievanceForwardingService.takeHearing(grievanceForwardingNoteDTO, authentication);
        Map<String, Object> response = new HashMap<>();

        System.out.println("genericresponse "+genericResponse);

        if (genericResponse.isSuccess()) {

            Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(mobileTakeHearingForwardingDTO.getGrievanceId());
            Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
            Object allComplaintDetails = data.get("allComplaintDetails");

            response.put("data", allComplaintDetails);
            response.put("status", "success");
            response.put("message", "Hearing taken successfully.");
            return response;
        } else {
            response.put("status", "error");
            response.put("message", "Failed to take hearing");
            return response;
        }


    }
    public Map<String, Object> forwardToAnotherOffice(Authentication authentication,
                                                      Long complaint_id,
                                                      Long office_id,
                                                      String note,
                                                      String other_service,
                                                      Long service_id,
                                                      String username,
                                                      List<MultipartFile> files,
                                                      String file_name_by_user,
                                                      Principal principal) throws ParseException {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        List<FileDTO> convertedFiles = null;
        if (files != null && !files.isEmpty()) {
            convertedFiles = fileUploadUtil.getFileDTOFromMultipart(files, file_name_by_user, principal);
        }


        MobileGrievanceForwardingRequest mobileGrievanceForwardingRequest = MobileGrievanceForwardingRequest.builder()
                .complaint_id(complaint_id)
                .office_id(office_id)
                .note(note)
                .other_service(other_service)
                .service_id(service_id)
                .username(username)
                .files(convertedFiles)
                .file_name_by_user(file_name_by_user)
                .build();

        ForwardToAnotherOfficeDTO forwardToAnotherOfficeDTO = ForwardToAnotherOfficeDTO.builder()
                .grievanceId(mobileGrievanceForwardingRequest.getComplaint_id())
                .officeId(mobileGrievanceForwardingRequest.getOffice_id())
                .citizenCharterId(mobileGrievanceForwardingRequest.getService_id())
                .note(mobileGrievanceForwardingRequest.getNote())
                .otherServiceName(mobileGrievanceForwardingRequest.getOther_service())
                .currentStatus(GrievanceCurrentStatus.FORWARDED_OUT)
                .build();
        GenericResponse genericResponse = grievanceForwardingService.forwardGrievanceToAnotherOffice(forwardToAnotherOfficeDTO, userInformation);
        Map<String, Object> response = new HashMap<>();

        if (genericResponse.isSuccess()) {

            Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(mobileGrievanceForwardingRequest.getComplaint_id());
            Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
            Object allComplaintDetails = data.get("allComplaintDetails");

            response.put("data", allComplaintDetails);
            response.put("status", "success");
            response.put("message", "The grievance has been forwarded successfully.");
            return response;
        } else {
            response.put("status", "error");
            response.put("message", "Grievance forwarding error while forwarding to another office.");
            return response;
        }
    }

    public Map<String, Object> rejectGrievance(Authentication authentication,
                                               Long complaint_id,
                                               Long office_id,
                                               String username,
                                               String note,
                                               String fileNameByUser,
                                               List<MultipartFile> files,
                                               Principal principal) throws ParseException {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        List<FileDTO> convertedFiles = null;
        if (files != null && !files.isEmpty()) {
            convertedFiles = fileUploadUtil.getFileDTOFromMultipart(files, fileNameByUser, principal);
        }

        MobileGrievanceForwardingRequest mobileGrievanceForwardingRequest = MobileGrievanceForwardingRequest.builder()
                .complaint_id(complaint_id)
                .office_id(office_id)
                .note(note)
                .username(username)
                .files(convertedFiles)
                .file_name_by_user(fileNameByUser)
                .build();


        GrievanceForwardingNoteDTO grievanceRejectionForwardingNote = GrievanceForwardingNoteDTO.builder()
                .grievanceId(complaint_id)
                .note(note)
                .currentStatus(null)
                .files(convertedFiles)
                .referredFiles(null)
                .build();
        GenericResponse genericResponse = grievanceForwardingService.rejectGrievance(userInformation, grievanceRejectionForwardingNote);

        Map<String, Object> response = new HashMap<>();
        if (genericResponse.isSuccess()) {
            Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(mobileGrievanceForwardingRequest.getComplaint_id());
            Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
            Object allComplaintDetails = data.get("allComplaintDetails");

            response.put("data", allComplaintDetails);
            response.put("status", "success");
            response.put("message", "The grievance has been rejected successfully.");
            return response;
        } else {
            response.put("status", "error");
            response.put("message", "Grievance rejection error.");
            return response;
        }
    }

    public Map<String, Object> sendToAppealOfficerOrSubordinateOffice(
            Authentication authentication,
            Long complaint_id,
            String note,
            Long office_id,
            String other_service,
            Long service_id,
            List<MultipartFile> files,
            String fileNameByUser
    ) throws ParseException {
        GrievanceForwardingNoteDTO grievanceForwardingNoteDTO = GrievanceForwardingNoteDTO.builder()
                .grievanceId(complaint_id)
                .note(note)
                .build();

        if (office_id == null) {
            GenericResponse genericResponse = grievanceForwardingService.sendToAppealOfficer(authentication, grievanceForwardingNoteDTO);

            Map<String, Object> response = new HashMap<>();
            if (genericResponse.isSuccess()) {

                Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(complaint_id);
                Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
                Object allComplaintDetails = data.get("allComplaintDetails");

                response.put("data", allComplaintDetails);
                response.put("status", "success");
                response.put("message", "The grievance has been sent to appeal officer successfully.");
                return response;
            } else {
                response.put("status", "error");
                response.put("data", null);
                response.put("message", "Grievance could not be send to appeal officer.");
                return response;
            }
        }

        ForwardToAnotherOfficeDTO forwardToAnotherOfficeDTO = ForwardToAnotherOfficeDTO.builder()
                .grievanceId(complaint_id)
                .officeId(office_id)
                .note(note)
                .otherServiceName(other_service)
                .currentStatus(GrievanceCurrentStatus.FORWARDED_IN)
                .build();

        GenericResponse genericResponse = grievanceForwardingService.forwardGrievanceToAnotherOffice(forwardToAnotherOfficeDTO, Utility.extractUserInformationFromAuthentication(authentication));

        Map<String, Object> response = new HashMap<>();
        if (genericResponse.isSuccess()) {

            Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(complaint_id);
            Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
            Object allComplaintDetails = data.get("allComplaintDetails");

            response.put("data", allComplaintDetails);
            response.put("status", "success");
            response.put("message", "The grievance has been sent to subordinate office successfully.");
            return response;
        } else {
            response.put("status", "error");
            response.put("data", null);
            response.put("message", "Grievance could not be send to subordinate office.");
            return response;
        }
    }

    public Map<String, Object> requestDocument(Authentication authentication, Long complaintId, String note) throws ParseException {
        InvestigationMaterialHearingDTO materialHearingDTO = InvestigationMaterialHearingDTO.builder()
                .grievanceId(complaintId)
                .note(note)
                .hearingDate(null)
                .persons(Collections.singletonList("COMPLAINANT"))
                .build();
        grievanceForwardingService.requestEvidences(materialHearingDTO, authentication);
        MobileGrievanceResponseDTO grievance = mobileGrievanceService.findGrievancesById(complaintId);

        if (grievance == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", null);
            response.put("status", "success");

            return response;
        }

        Map<String, Object> grievanceDetails = new LinkedHashMap<>();
        grievanceDetails.put("id", grievance.getId());
        grievanceDetails.put("submission_date", grievance.getSubmission_date());
        grievanceDetails.put("submission_date_bn", BanglaConverter.getDateBanglaFromEnglish(grievance.getSubmission_date()));
        grievanceDetails.put("complaint_type", grievance.getComplaint_type());
        grievanceDetails.put("complaint_type_bn", convertServiceTypeToBangla(ServiceType.valueOf(grievance.getComplaint_type_bn())));
        grievanceDetails.put("current_status", grievance.getCurrent_status());
        grievanceDetails.put("current_status_bn", convertGrievanceStatusToBangla(GrievanceCurrentStatus.valueOf(grievance.getCurrent_status_bn())));
        grievanceDetails.put("subject", grievance.getSubject());
        grievanceDetails.put("details", grievance.getDetails());
        grievanceDetails.put("grievance_from", grievance.getGrievance_from());
        grievanceDetails.put("tracking_number", grievance.getTracking_number());
        grievanceDetails.put("tracking_number_bn", BanglaConverter.convertToBanglaDigit(grievance.getTracking_number()));
        grievanceDetails.put("complainant_id", grievance.getComplainant_id());
        grievanceDetails.put("mygov_user_id", grievance.getMygov_user_id());
        grievanceDetails.put("triple_three_agent_id", grievance.getTriple_three_agent_id());
        grievanceDetails.put("is_grs_user", grievance.getIs_grs_user());
        grievanceDetails.put("office_id", grievance.getOffice_id());
        grievanceDetails.put("service_id", grievance.getService_id());
        grievanceDetails.put("service_id_before_forward", grievance.getService_id_before_forward());
        grievanceDetails.put("current_appeal_office_id", grievance.getCurrent_appeal_office_id());
        grievanceDetails.put("current_appeal_office_unit_organogram_id", grievance.getCurrent_appeal_office_unit_organogram_id());
        grievanceDetails.put("send_to_ao_office_id", grievance.getSend_to_ao_office_id());
        grievanceDetails.put("is_anonymous", grievance.getIs_anonymous());
        grievanceDetails.put("case_number", grievance.getCase_number());
        grievanceDetails.put("other_service", grievance.getOther_service());
        grievanceDetails.put("other_service_before_forward", grievance.getOther_service_before_forward());
        grievanceDetails.put("service_receiver", grievance.getService_receiver());
        grievanceDetails.put("service_receiver_relation", grievance.getService_receiver_relation());
        grievanceDetails.put("gro_decision", grievance.getGro_decision());
        grievanceDetails.put("gro_identified_complaint_cause", grievance.getGro_identified_complaint_cause());
        grievanceDetails.put("gro_suggestion", grievance.getGro_suggestion());
        grievanceDetails.put("ao_decision", grievance.getAo_decision());
        grievanceDetails.put("ao_identified_complaint_cause", grievance.getAo_identified_complaint_cause());
        grievanceDetails.put("ao_suggestion", grievance.getAo_suggestion());
        grievanceDetails.put("created_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(grievance.getCreated_at())));
        grievanceDetails.put("updated_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(grievance.getUpdated_at())));
        grievanceDetails.put("created_by", grievance.getCreated_by());
        grievanceDetails.put("modified_by", grievance.getModified_by());
        grievanceDetails.put("status", grievance.getStatus());
        grievanceDetails.put("rating", grievance.getRating());
        grievanceDetails.put("appeal_rating", grievance.getAppeal_rating());
        grievanceDetails.put("is_rating_given", grievance.getIs_rating_given());
        grievanceDetails.put("is_appeal_rating_given", grievance.getIs_appeal_rating_given());
        grievanceDetails.put("feedback_comments", grievance.getFeedback_comments());
        grievanceDetails.put("appeal_feedback_comments", grievance.getAppeal_feedback_comments());
        grievanceDetails.put("source_of_grievance", grievance.getSource_of_grievance());
        grievanceDetails.put("is_offline_complaint", grievance.getIs_offline_complaint());
        grievanceDetails.put("is_self_motivated_grievance", grievance.getIs_self_motivated_grievance());
        grievanceDetails.put("uploader_office_unit_organogram_id", grievance.getUploader_office_unit_organogram_id());
        grievanceDetails.put("possible_close_date", grievance.getPossible_close_date());
        grievanceDetails.put("possible_close_date_bn", getDateBanglaFromEnglish(grievance.getPossible_close_date_bn()));
        grievanceDetails.put("is_evidence_provide", grievance.getIs_evidence_provide());
        grievanceDetails.put("is_see_hearing_date", grievance.getIs_see_hearing_date());
        grievanceDetails.put("is_safety_net", grievance.getIs_safety_net());
        grievanceDetails.put("complaint_category", grievance.getComplaint_category());
        grievanceDetails.put("sp_programme_id", grievance.getSp_programme_id());
        grievanceDetails.put("geo_division_id", grievance.getGeo_division_id());
        grievanceDetails.put("geo_district_id", grievance.getGeo_district_id());
        grievanceDetails.put("geo_upazila_id", grievance.getGeo_upazila_id());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", grievanceDetails);
        response.put("status", "success");

        return response;
    }

    public Map<String, Object> giveGuidelinesToProvidingServices(Authentication authentication, Long complaintId, Long officeId, String note, String deadline, String guidanceReceiver) throws ParseException, IOException {


        ObjectMapper obj = new ObjectMapper();
        obj.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MobileOfficerGuidServDTO guidanceReceiverObj = obj.readValue(guidanceReceiver, MobileOfficerGuidServDTO.class);


        Office office = officeRepo.findOfficeById(officeId);
        GrievanceForwardingGuidanceForServiceDTO grievanceForwardingGuidanceForServiceDTO = GrievanceForwardingGuidanceForServiceDTO.builder()
                .grievanceId(complaintId)
                .guidanceReceiver("post_" + office.getOfficeMinistry().getId() + "_" + officeId + "_" + guidanceReceiverObj.getOffice_unit_organogram_id())
                .note(note + " সেবা প্রদানের শেষ তারিখ: " + BanglaConverter.getDateBanglaFromEnglish(deadline))
                .build();

        GenericResponse genericResponse = grievanceForwardingService.giveGuidanceToGiveService(authentication, grievanceForwardingGuidanceForServiceDTO);

        Map<String, Object> response = new HashMap<>();
        if (genericResponse.isSuccess()) {

            Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(complaintId);
            Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
            Object allComplaintDetails = data.get("allComplaintDetails");

            response.put("data", allComplaintDetails);
            response.put("status", "success");
            response.put("message", "Grievance has been successfully forwarded for further action.");
            return response;
        } else {
            response.put("status", "error");
            response.put("data", null);
            response.put("message", "Failed to forward the grievance for further action. Please try again or contact support.");
            return response;
        }
    }

    public Map<String, Object> askForPermission(Authentication authentication, Long complaintId, String note) throws ParseException {
        GenericResponse genericResponse = grievanceForwardingService.askPermission(authentication,
                GrievanceForwardingNoteDTO.builder()
                        .grievanceId(complaintId)
                        .note(note)
                        .build()
        );

        Map<String, Object> response = new LinkedHashMap<>();
        if (genericResponse.isSuccess()) {
            MobileGrievanceResponseDTO grievance = mobileGrievanceService.findGrievancesById(complaintId);
            response.put("status", "success");
            response.put("data", mobileGrievanceService.getGrievanceDetails(grievance));
            response.put("message", "The grievance has been successfully sent for requesting permission.");
            return response;

        } else {
            response.put("status", "error");
            response.put("data", null);
            response.put("message", "Failed to sent the grievance for requesting permission. Please try again or contact support.");
            return response;
        }
    }

    public Map<String, Object> provideEvidence(
            Long complaintId,
            String note,
            List<MultipartFile> files,
            String fileNameByUser,
            Authentication authentication,
            Principal principal
    ) throws ParseException {
        List<FileDTO> convertedFiles = null;
        if (files != null && !files.isEmpty()) {
            convertedFiles = fileUploadUtil.getFileDTOFromMultipart(files, fileNameByUser, principal);
        }
        GrievanceForwardingNoteDTO grievanceForwardingNoteDTO = GrievanceForwardingNoteDTO.builder()
                .grievanceId(complaintId)
                .note(note)
                .files(convertedFiles)
                .referredFiles(new ArrayList<>())
                .build();
        GenericResponse genericResponse = grievanceForwardingService.addFileTransitToHearing(grievanceForwardingNoteDTO, authentication);

        Map<String, Object> response = new HashMap<>();
        if (genericResponse.isSuccess()) {

            Map<String, Object> complaintDetails = mobileGrievanceService.getComplaintDetailsById(complaintId);
            Map<String, Object> data = (Map<String, Object>) complaintDetails.get("data");
            Object allComplaintDetails = data.get("allComplaintDetails");

            response.put("data", allComplaintDetails);
            response.put("status", "success");
            response.put("message", "Additional evidence has been successfully provided.");
            return response;
        } else {
            response.put("status", "error");
            response.put("data", null);
            response.put("message", "Failed to provide additional evidence for the complaint.");
            return response;
        }
    }
    public Map<String, Object> hearingNotice(Authentication authentication, Long complaintId, String hearingDate, String hearingTime, String note) throws ParseException {
        // Parse the date (e.g., "2024-12-31")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date parsedDate = dateFormat.parse(hearingDate);
        // Extract the time components from hearingTime
        // Assuming hearingTime contains "14:11:59" somewhere in the string
        String timeString = extractTimeFromHearingTime(hearingTime);
        if (timeString == null) {
            throw new ParseException("Unable to extract time from hearingTime", 0);
        }
        // Parse the time (e.g., "14:11:59")
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date parsedTime = timeFormat.parse(timeString);
        // Combine date and time
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parsedDate);
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(parsedTime);

        // Set time components from parsedTime into calendar
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, 0);
        Date finalDateTime = calendar.getTime();
        // Proceed with your logic using finalDateTime
        InvestigationMaterialHearingDTO investigationMaterialHearingDTO = InvestigationMaterialHearingDTO.builder()
                .grievanceId(complaintId)
                .note(note)
                .persons(Collections.singletonList("COMPLAINANT"))
                .hearingDate(finalDateTime)
                .build();

        GenericResponse genericResponse = grievanceForwardingService.askForHearing(investigationMaterialHearingDTO, authentication);

        Map<String, Object> response = new LinkedHashMap<>();
        if (genericResponse.isSuccess()) {
            MobileGrievanceResponseDTO grievance = mobileGrievanceService.findGrievancesById(complaintId);
            response.put("status", "success");
            response.put("data", mobileGrievanceService.getGrievanceDetails(grievance));
            response.put("message", "The grievance has been successfully sent for hearing notice.");
            return response;

        } else {
            response.put("status", "error");
            response.put("data", null);
            response.put("message", "Failed to sent the grievance for hearing notice. Please try again or contact support.");
            return response;
        }
    }

    private String extractTimeFromHearingTime(String hearingTime) {
        // Use regex to find HH:mm:ss in hearingTime
        Pattern timePattern = Pattern.compile("\\b(\\d{2}:\\d{2}:\\d{2})\\b");
        Matcher matcher = timePattern.matcher(hearingTime);
        if (matcher.find()) {
            return matcher.group(1); // e.g., "14:11:59"
        }
        return null;
    }
}
