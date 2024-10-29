package com.grs.mobileApp.controller;

import com.grs.api.model.request.SuggestionRequestDTO;
import com.grs.core.domain.grs.Suggestion;
import com.grs.core.service.MessageService;
import com.grs.core.service.SuggestionService;
import com.grs.mobileApp.dto.MobileSuggestionRequestDTO;
import com.grs.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MobileSuggestionController {

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private MessageService messageService;

    @PostMapping("/api/provide-suggestion")
    public ResponseEntity<Map<String, String>> provideSuggestion(
            @ModelAttribute MobileSuggestionRequestDTO mobileDTO) {

        Map<String, String> response = new HashMap<>();
        String message;
        String status;

        // Map mobile DTO to existing DTO
        SuggestionRequestDTO suggestionRequestDTO = mapToSuggestionRequestDTO(mobileDTO);

        // Reuse existing validation logic
        if (suggestionRequestDTO.getOfficeId() == null) {
            message = messageService.isCurrentLanguageInEnglish() ? "Error! Office selection is not valid" : "দুঃখিত দপ্তর নির্বাচন সঠিক নয়";
            status = "error";
        } else if (suggestionRequestDTO.getOfficeServiceId() == null
                && !StringUtil.isValidString(suggestionRequestDTO.getOfficeServiceName())) {
            message = messageService.isCurrentLanguageInEnglish() ? "Error! Service selection is not valid" : "দুঃখিত সেবা নির্বাচন সঠিক নয়";
            status = "error";
        } else {
            // Use existing service method
            Suggestion suggestion = suggestionService.addSuggestion(suggestionRequestDTO);
            if (suggestion == null) {
                message = messageService.isCurrentLanguageInEnglish() ? "Error! Cannot submit suggestion" : "দুঃখিত! পরামর্শ প্রদান করা যাচ্ছেনা";
                status = "error";
            } else {
                message = messageService.isCurrentLanguageInEnglish() ? "Your suggestion has been submitted" : "আপনার মতামত গৃহীত হয়েছে";
                status = "success";
            }
        }

        response.put("status", status);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    private SuggestionRequestDTO mapToSuggestionRequestDTO(MobileSuggestionRequestDTO mobileSuggestionRequestDTO) {
        SuggestionRequestDTO suggestionRequestDTO = new SuggestionRequestDTO();
        suggestionRequestDTO.setName(mobileSuggestionRequestDTO.getName());
        suggestionRequestDTO.setEmail(mobileSuggestionRequestDTO.getEmail());
        suggestionRequestDTO.setPhone(mobileSuggestionRequestDTO.getPhone());
        suggestionRequestDTO.setOfficeId(mobileSuggestionRequestDTO.getOffice_id());
        suggestionRequestDTO.setOfficeServiceId(mobileSuggestionRequestDTO.getOffice_service_id());
        suggestionRequestDTO.setOfficeServiceName(mobileSuggestionRequestDTO.getOffice_service_name());
        suggestionRequestDTO.setDescription(mobileSuggestionRequestDTO.getDescription());
        suggestionRequestDTO.setSuggestion(mobileSuggestionRequestDTO.getSuggestion());
        suggestionRequestDTO.setTypeOfSuggestionForImprovement(mobileSuggestionRequestDTO.getType_of_opinion());
        suggestionRequestDTO.setEffectTowardsSolution(mobileSuggestionRequestDTO.getProbable_improvement());
        return suggestionRequestDTO;
    }
}
