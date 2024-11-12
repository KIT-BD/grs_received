package com.grs.mobileApp.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grs.api.model.request.FileDTO;
import com.grs.api.model.request.OpinionRequestDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.mobileApp.dto.MobileGrievanceForwardingRequest;
import com.grs.mobileApp.dto.MobileOfficerDTO;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MobileGrievanceForwardingService {

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
}
