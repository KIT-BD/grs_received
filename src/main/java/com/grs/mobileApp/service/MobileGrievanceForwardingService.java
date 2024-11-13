package com.grs.mobileApp.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grs.api.model.UserInformation;
import com.grs.api.model.request.FileDTO;
import com.grs.api.model.request.ForwardToAnotherOfficeDTO;
import com.grs.api.model.request.GrievanceForwardingNoteDTO;
import com.grs.api.model.request.OpinionRequestDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.service.GrievanceForwardingService;
import com.grs.mobileApp.dto.MobileGrievanceForwardingRequest;
import com.grs.mobileApp.dto.MobileOfficerDTO;
import com.grs.utils.Utility;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MobileGrievanceForwardingService {

    private GrievanceForwardingService grievanceForwardingService;

    public Map<String,Object> sendForOpinion(
            Authentication authentication,
            MobileGrievanceForwardingRequest mobileGrievanceForwardingRequest)
    {

        ObjectMapper objectMapper = new ObjectMapper();
        List<MobileOfficerDTO> officerDTOList = null;
        try {
            officerDTOList = objectMapper.readValue(mobileGrievanceForwardingRequest.getOfficers(), new TypeReference<List<MobileOfficerDTO>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }

        Optional<MobileOfficerDTO> primary = officerDTOList.stream().filter(officer -> officer.getReceiverCheck() && !officer.getCcCheck()).findFirst();

        List<MobileOfficerDTO> filteredList = officerDTOList.stream()
                .filter(officer -> !officer.getReceiverCheck() && officer.getCcCheck())
                .collect(Collectors.toList());


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate localDate = LocalDate.parse(mobileGrievanceForwardingRequest.getDeadline(), formatter);
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        OpinionRequestDTO ReqToOp = OpinionRequestDTO.builder()
                .grievanceId(mobileGrievanceForwardingRequest.getComplaint_id())
                .comment(mobileGrievanceForwardingRequest.getNote())
                .files(mobileGrievanceForwardingRequest.getFiles())
                .postNode(null)
                .ccNode(null)
                .deadline(date)
                .referredFiles(null)
                .build();

        if (!(ReqToOp.getPostNode() != null
                && !ReqToOp.getPostNode().isEmpty()
                && ReqToOp.getPostNode().size() <= 1
                && ReqToOp.getPostNode().get(0) != null)) {
                System.out.println("অনুগ্রহ করে মতামতের জন্য অন্ততপক্ষে যে কোন একজনকে নির্বাচন করুন");
        }

        if (ReqToOp.getCcNode() != null) {
            List<String> ccNodeList = ReqToOp.getCcNode();
            for (String ccNode : ccNodeList) {
                if (ccNode.equals(ReqToOp.getPostNode().get(0))) {
                    System.out.println("অনুগ্রহ করে প্রধান প্রাপক ব্যতীত অন্য একজনকে অনুলিপি প্রাপক হিসেবে নির্বাচন করুন");
                }
            }
        }
        grievanceForwardingService.sendForOpinion(authentication, ReqToOp);

        return null;
    }



    public Map<String, Object> forwardToAnotherOffice(Authentication authentication, MobileGrievanceForwardingRequest mobileGrievanceForwardingRequest) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
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
        if (genericResponse.isSuccess()){
            response.put("status", "success");
            response.put("message", "The grievance has been forwarded successfully.");
            return response;
        }
        else {
            response.put("status", "error");
            response.put("message", "Grievance forwarding error while forwarding to another office.");
            return response;
        }
    }

    public Map<String, Object> rejectGrievance(Authentication authentication,
                                               Long complaint_id,
                                               Long office_id,
                                               Long username,
                                               String note,
                                               String fileNameByUser,
                                               List<MultipartFile> files,
                                               Principal principal) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        GrievanceForwardingNoteDTO comment = GrievanceForwardingNoteDTO.builder().build();
        GenericResponse genericResponse = grievanceForwardingService.rejectGrievance(userInformation, comment);
        Map<String, Object> response = new HashMap<>();
        if (genericResponse.isSuccess()){
            response.put("status", "success");
            response.put("message", "The grievance has been rejected successfully.");
            return response;
        }
        else {
            response.put("status", "error");
            response.put("message", "Grievance rejection error.");
            return response;
        }
    }
}
