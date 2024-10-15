package com.grs.api.controller;

import com.grs.api.model.response.grievance.ReturnedGrievanceDTO;
import com.grs.core.domain.IdentificationType;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.Grievance;
import com.grs.core.repo.grs.GrievanceRepo;
import com.grs.core.repo.projapoti.OfficeRepo;
import com.grs.core.service.ModelViewService;
import com.grs.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchTrxController {

    private final GrievanceRepo grievanceRepo;
    private final OfficeRepo officeRepo;
    @GetMapping("/trx/{trx_id}")
    public ResponseEntity<?> findByTrxId(@PathVariable("trx_id") String trx_id) {
        List<Grievance> grievances = grievanceRepo.findGrievancesByTrackingNumber(trx_id);

        List<ReturnedGrievanceDTO> returnedGrievanceDTOList = grievances.stream().map(this::grievanceToDTOMapper).collect(Collectors.toList());

        if (returnedGrievanceDTOList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Grievance with tracking number " + trx_id + " not found.");
        }

        if (returnedGrievanceDTOList.size() == 1) {
            Long grievance_id = returnedGrievanceDTOList.get(0).getId();
            Map<String, Object> response = new HashMap<>();
            response.put("single", true);
            response.put("id", grievance_id);
            return ResponseEntity.ok(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("single", false);
        response.put("grievances", returnedGrievanceDTOList);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nid/{nid}")
    public ResponseEntity<?> findByNID(@PathVariable("nid") String nid) {
        List<Grievance> grievances = grievanceRepo.findGrievancesByIdentificationValue(String.valueOf(IdentificationType.NID),nid);

        List<ReturnedGrievanceDTO> returnedGrievanceDTOList = grievances.stream().map(this::grievanceToDTOMapper).collect(Collectors.toList());

        if (returnedGrievanceDTOList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Grievance with nid number " + nid + " not found.");
        }

        if (returnedGrievanceDTOList.size() == 1) {
            Long grievance_id = returnedGrievanceDTOList.get(0).getId();
            Map<String, Object> response = new HashMap<>();
            response.put("single", true);
            response.put("id", grievance_id);
            return ResponseEntity.ok(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("single", false);
        response.put("grievances", returnedGrievanceDTOList);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/ph/{ph}")
    public ResponseEntity<?> findByPhoneNumber(@PathVariable("ph") String ph) {
        List<Grievance> grievances = grievanceRepo.findGrievancesByMobileNumber(ph);

        List<ReturnedGrievanceDTO> returnedGrievanceDTOList = grievances.stream().map(this::grievanceToDTOMapper).collect(Collectors.toList());

        if (returnedGrievanceDTOList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Grievance with phone number " + ph + " not found.");
        }

        if (returnedGrievanceDTOList.size() == 1) {
            Long grievance_id = returnedGrievanceDTOList.get(0).getId();
            Map<String, Object> response = new HashMap<>();
            response.put("single", true);
            response.put("id", grievance_id);
            return ResponseEntity.ok(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("single", false);
        response.put("grievances", returnedGrievanceDTOList);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/bcn/{bcn}")
    public ResponseEntity<?> findByBCN(@PathVariable("bcn") String bcn) {
        List<Grievance> grievances = grievanceRepo.findGrievancesByIdentificationValue(String.valueOf(IdentificationType.BCN),bcn);

        List<ReturnedGrievanceDTO> returnedGrievanceDTOList = grievances.stream().map(this::grievanceToDTOMapper).collect(Collectors.toList());

        if (returnedGrievanceDTOList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Grievance with bcn number " + bcn + " not found.");
        }

        if (returnedGrievanceDTOList.size() == 1) {
            Long grievance_id = returnedGrievanceDTOList.get(0).getId();
            Map<String, Object> response = new HashMap<>();
            response.put("single", true);
            response.put("id", grievance_id);
            return ResponseEntity.ok(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("single", false);
        response.put("grievances", returnedGrievanceDTOList);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/pp/{pp}")
    public ResponseEntity<?> findByPassport(@PathVariable("pp") String pp) {
        List<Grievance> grievances = grievanceRepo.findGrievancesByIdentificationValue(String.valueOf(IdentificationType.PASSPORT),pp);

        List<ReturnedGrievanceDTO> returnedGrievanceDTOList = grievances.stream().map(this::grievanceToDTOMapper).collect(Collectors.toList());

        if (returnedGrievanceDTOList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Grievance with passport number " + pp + " not found.");
        }

        if (returnedGrievanceDTOList.size() == 1) {
            Long grievance_id = returnedGrievanceDTOList.get(0).getId();
            Map<String, Object> response = new HashMap<>();
            response.put("single", true);
            response.put("id", grievance_id);
            return ResponseEntity.ok(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("single", false);
        response.put("grievances", returnedGrievanceDTOList);
        return ResponseEntity.ok(response);
    }

    private ReturnedGrievanceDTO grievanceToDTOMapper(Grievance grievance){
        ReturnedGrievanceDTO dto = new ReturnedGrievanceDTO();
        dto.setId(grievance.getId());
        dto.setSubmissionDate(grievance.getSubmissionDate());
        dto.setGrievanceType(grievance.getGrievanceType());
        dto.setGrievanceCurrentStatus(grievance.getGrievanceCurrentStatus());
        dto.setSubject(grievance.getSubject());
        dto.setDetails(grievance.getDetails());
        dto.setTrackingNumber(grievance.getTrackingNumber());
        dto.setOfficeId(grievance.getOfficeId());
        dto.setServiceOrigin(grievance.getServiceOrigin());
        dto.setAttachedFiles(grievance.getAttachedFiles());
        dto.setComplainantId(grievance.getComplainantId());
        dto.setAnonymous(grievance.isAnonymous());
        dto.setGrsUser(grievance.isGrsUser());
        dto.setCaseNumber(grievance.getCaseNumber());
        dto.setOtherService(grievance.getOtherService());
        dto.setOtherServiceBeforeForward(grievance.getOtherServiceBeforeForward());
        dto.setServiceReceiver(grievance.getServiceReceiver());
        dto.setServiceReceiverRelation(grievance.getServiceReceiverRelation());
        dto.setCurrentAppealOfficeId(grievance.getCurrentAppealOfficeId());
        dto.setSendToAoOfficeId(grievance.getSendToAoOfficeId());
        dto.setCurrentAppealOfficerOfficeUnitOrganogramId(grievance.getCurrentAppealOfficerOfficeUnitOrganogramId());
        dto.setGroDecision(grievance.getGroDecision());
        dto.setGroIdentifiedCause(grievance.getGroIdentifiedCause());
        dto.setGroSuggestion(grievance.getGroSuggestion());
        dto.setAppealOfficerDecision(grievance.getAppealOfficerDecision());
        dto.setAppealOfficerIdentifiedCause(grievance.getAppealOfficerIdentifiedCause());
        dto.setAppealOfficerSuggestion(grievance.getAppealOfficerSuggestion());
        dto.setRating(grievance.getRating());
        dto.setAppealRating(grievance.getAppealRating());
        dto.setIsRatingGiven(grievance.getIsRatingGiven());
        dto.setIsAppealRatingGiven(grievance.getIsAppealRatingGiven());
        dto.setFeedbackComments(grievance.getFeedbackComments());
        dto.setAppealFeedbackComments(grievance.getAppealFeedbackComments());
        dto.setIsOfflineGrievance(grievance.getIsOfflineGrievance());
        dto.setUploaderOfficeUnitOrganogramId(grievance.getUploaderOfficeUnitOrganogramId());
        dto.setIsSelfMotivatedGrievance(grievance.getIsSelfMotivatedGrievance());
        dto.setSourceOfGrievance(grievance.getSourceOfGrievance());
        dto.setSafetyNet(grievance.isSafetyNet());
        dto.setOfficeLayers(grievance.getOfficeLayers());
        dto.setComplaintCategory(grievance.getComplaintCategory());
        dto.setSpProgrammeId(grievance.getSpProgrammeId());
        dto.setGeoDivisionId(grievance.getGeoDivisionId());
        dto.setGeoDistrictId(grievance.getGeoDistrictId());
        dto.setGeoUpazilaId(grievance.getGeoUpazilaId());
        dto.setMediumOfSubmission(grievance.getMediumOfSubmission());
        dto.setOfficeName(officeRepo.findOfficeById(grievance.getOfficeId()).getNameBangla());

        return dto;
    }

}
