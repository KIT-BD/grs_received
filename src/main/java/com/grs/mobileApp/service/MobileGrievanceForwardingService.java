package com.grs.mobileApp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grs.api.model.UserInformation;
import com.grs.api.model.request.FileDTO;
import com.grs.api.model.request.ForwardToAnotherOfficeDTO;
import com.grs.api.model.request.GrievanceForwardingNoteDTO;
import com.grs.api.model.request.OpinionRequestDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.api.model.response.grievanceForwarding.GrievanceForwardingInvestigationDTO;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.repo.projapoti.OfficeRepo;
import com.grs.core.service.GrievanceForwardingService;
import com.grs.mobileApp.dto.*;
import com.grs.utils.FileUploadUtil;
import com.grs.utils.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

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
//        System.out.println("Officer DTO list"+officerDTOList.get(0).getReceiverCheck());
//        System.out.println("Officer DTO list"+officerDTOList.get(1).getReceiverCheck());
        Optional<MobileOfficerInvstDTO> primary = officerDTOList.stream().filter(officer -> officer.isCommitteeHead()).findFirst();

        List<MobileOfficerInvstDTO> comitteeList = officerDTOList.stream()
                .filter(officer -> !officer.isCommitteeHead())
                .collect(Collectors.toList());


        assert primary.isPresent();
        Long postNodeMinistryId = officeRepo.findOfficeById(primary.get().getOffice_id()).getOfficeMinistry().getId();


        String head="post_" + postNodeMinistryId + "_" + primary.get().getOffice_id() + "_" + primary.get().getOffice_unit_organogram_id();

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


        GenericResponse genericResponse= grievanceForwardingService.initiateInvestigation(grievanceForwardingInvestigationDTO, authentication);

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




    public  Map<String, Object> giveOpinion(MobileOpinionForwardingDTO mobileOpinionForWardingDTO,
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

    //================================================================================================================================================


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

}
