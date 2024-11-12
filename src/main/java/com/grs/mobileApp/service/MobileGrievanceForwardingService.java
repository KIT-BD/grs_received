package com.grs.mobileApp.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grs.api.model.UserInformation;
import com.grs.api.model.request.FileDTO;
import com.grs.api.model.request.ForwardToAnotherOfficeDTO;
import com.grs.api.model.request.OpinionRequestDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.service.GrievanceForwardingService;
import com.grs.mobileApp.dto.MobileGrievanceForwardingRequest;
import com.grs.mobileApp.dto.MobileOfficerDTO;
import com.grs.utils.Utility;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MobileGrievanceForwardingService {

    private GrievanceForwardingService grievanceForwardingService;

    public Map<String,Object> sendForOpinion(Authentication authentication, MobileGrievanceForwardingRequest mobileGrievanceForwardingRequest) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        List<MobileOfficerDTO> listMobileOfficerDTO = objectMapper.readValue(
                (JsonParser) mobileGrievanceForwardingRequest.getOfficers(),
                new TypeReference<List<MobileOfficerDTO>>() {}
        );

        List<String> primaryRecipient;

        Optional<MobileOfficerDTO> primary = listMobileOfficerDTO.stream().filter(officer -> officer.getReceiver_check() && !officer.getCc_check()).findFirst();

        List<MobileOfficerDTO> filteredList = listMobileOfficerDTO.stream()
                .filter(officer -> !officer.getReceiver_check() && officer.getCc_check())
                .collect(Collectors.toList());


        if (primary.isPresent()){
            primary.get();
        }

        if (!filteredList.isEmpty()){

        }




//        OpinionRequestDTO ReqToOp = OpinionRequestDTO.builder()
//                .grievanceId(mobileGrievanceForwardingRequest.getComplaint_id())
//                .comment(mobileGrievanceForwardingRequest.getNote())
//                .files(mobileGrievanceForwardingRequest.getFiles())
//                .postNode()
//                .ccNode()
//                .deadline(mobileGrievanceForwardingRequest.getDeadline())
//                .referredFiles()
//                .build();

//        if (!(grievanceOpinionRequestDTO.getPostNode() != null
//                && !grievanceOpinionRequestDTO.getPostNode().isEmpty()
//                && grievanceOpinionRequestDTO.getPostNode().size() <= 1
//                && grievanceOpinionRequestDTO.getPostNode().get(0) != null)) {
//            return new GenericResponse(false, "অনুগ্রহ করে মতামতের জন্য অন্ততপক্ষে যে কোন একজনকে নির্বাচন করুন");
//        }
//
//        if (grievanceOpinionRequestDTO.getCcNode() != null) {
//            List<String> ccNodeList = grievanceOpinionRequestDTO.getCcNode();
//            for (String ccNode : ccNodeList) {
//                if (ccNode.equals(grievanceOpinionRequestDTO.getPostNode().get(0))) {
//                    return new GenericResponse(false, "অনুগ্রহ করে প্রধান প্রাপক ব্যতীত অন্য একজনকে অনুলিপি প্রাপক হিসেবে নির্বাচন করুন");
//                }
//            }
//        }
//        return this.grievanceForwardingService.sendForOpinion(authentication, grievanceOpinionRequestDTO);

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
            response.put("message", "Forwarded Successfully.");
            return response;
        }
        else {
            response.put("status", "success");
            response.put("message", "Grievance Movement Error.");
            return response;
        }
    }
}
