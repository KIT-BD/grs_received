package com.grs.mobileApp.controller;

import com.grs.api.model.response.GenericResponse;
import com.grs.mobileApp.dto.MobileGrievanceForwardingRequest;
import com.grs.mobileApp.service.MobileGrievanceForwardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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
    public  Map<String, Object> forwardToAnotherOffice(Authentication authentication, @RequestBody MobileGrievanceForwardingRequest mobileGrievanceForwardingRequest) {
        return mobileGrievanceForwardingService.forwardToAnotherOffice(authentication, mobileGrievanceForwardingRequest);
    }
}
