package com.grs.mobileApp.controller;

import com.grs.api.model.UserInformation;
import com.grs.api.model.response.grievance.ComplainantInfoBlacklistReqDTO;
import com.grs.api.model.response.grievance.ComplainantInfoDTO;
import com.grs.core.dao.BlacklistDAO;
import com.grs.core.domain.grs.Blacklist;
import com.grs.core.service.ComplainantService;
import com.grs.mobileApp.service.MobileGrievanceService;
import com.grs.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blacklist")
public class MobileBlacklistController {

    private final ComplainantService complainantService;
    private final BlacklistDAO blacklistDAO;
    private final MobileGrievanceService mobileGrievanceService;

    @PostMapping("/save")
    public Map<String, Object> saveAsBlacklist(
            @RequestParam Long complainant_id,
            @RequestParam Long office_id
    ){
        boolean done = this.complainantService.doBlacklistByComplainantId(complainant_id, office_id);

        Map<String, Object> response = new HashMap<>();
        if (done) {
            Blacklist blacklist = blacklistDAO.findByComplainantIdAndOfficeId(complainant_id, office_id);
            Map<String,Object> blacklistWrapper = new HashMap<>();
            blacklistWrapper.put("id", blacklist.getId());
            blacklistWrapper.put("complainant_id", blacklist.getComplainantId());
            blacklistWrapper.put("office_id", blacklist.getOfficeId());
            blacklistWrapper.put("requested", blacklist.getRequested());
            blacklistWrapper.put("blacklisted", blacklist.getBlacklisted());
            blacklistWrapper.put("office_name", blacklist.getOfficeName());
            blacklistWrapper.put("reason", blacklist.getReason());
            blacklistWrapper.put("created_at", blacklist.getCreatedAt());
            blacklistWrapper.put("modified_at", blacklist.getUpdatedAt());
            blacklistWrapper.put("created_by", blacklist.getCreatedBy());
            blacklistWrapper.put("modified_by", blacklist.getModifiedBy());
            blacklistWrapper.put("status", blacklist.getStatus());
            blacklistWrapper.put("complainant_info", mobileGrievanceService.getComplainantDetails(complainantService.findOne(complainant_id)));

            response.put("data", blacklistWrapper);
            response.put("status", "success");
            response.put("message", "The complainant has been added to blacklist.");
            return response;
        } else {
            response.put("status", "error");
            response.put("message", "The complainant is already blacklisted.");
            response.put("data", null);
            return response;
        }
    }

    @GetMapping("/index")
    public Map<String, Object> blacklists(
            @RequestParam Long office_id
    ){
        List<ComplainantInfoDTO> complainantInfoList = complainantService.getBlacklistByOfficeId(office_id);

        List<Blacklist> blacklists = new ArrayList<>();
        for(ComplainantInfoDTO c: complainantInfoList){
            blacklists.add(blacklistDAO.findByComplainantIdAndOfficeId(c.getId(), office_id));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", blacklists);
        response.put("status", "success");
        return response;
    }

    @GetMapping("/request")
    public Map<String, Object> requestedBlacklists(
            Authentication authentication
    ){

        if (authentication == null) {
            return null;
        }

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        List<ComplainantInfoBlacklistReqDTO> complainantInfoBlacklistReqDTOList = this.complainantService.getBlacklistRequestByChildOffices(
                userInformation.getOfficeInformation().getOfficeId(),
                userInformation.getOfficeInformation().getOfficeUnitOrganogramId());

        List<Blacklist> blacklists = new ArrayList<>();
        for(ComplainantInfoBlacklistReqDTO c: complainantInfoBlacklistReqDTOList){
            blacklists.addAll(blacklistDAO.findByComplainantId(c.getId()));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", blacklists);
        response.put("status", "success");
        return response;
    }

    @PostMapping("/status/{id}")
    public Map<String, Object> blacklistStatus(
            @PathVariable Long id,
            @RequestParam Integer blacklisted
    ){
        Blacklist foundBlacklist = blacklistDAO.findById(id);
        if (blacklisted == 1) {
            foundBlacklist.setBlacklisted(true);
            blacklistDAO.save(foundBlacklist);
            Map<String, Object> response = new HashMap<>();
            response.put("data", 1);
            response.put("status", "success");
            return response;
        }
        else if (blacklisted == 0) {
            foundBlacklist.setBlacklisted(false);
            blacklistDAO.save(foundBlacklist);
            Map<String, Object> response = new HashMap<>();
            response.put("data", 0);
            response.put("status", "success");
            return response;
        }
        else
            return null;

    }
}
