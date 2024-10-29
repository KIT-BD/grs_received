package com.grs.mobileApp.dto;

import com.grs.core.domain.EffectsTowardsSolution;
import com.grs.core.domain.ImprovementSuggestion;
import lombok.Data;

@Data
public class MobileSuggestionRequestDTO {
    private String name;
    private String email;
    private String phone;
    private Long office_id;
    private Long office_service_id;
    private String office_service_name;
    private String description;
    private String suggestion;
    private ImprovementSuggestion type_of_opinion;
    private EffectsTowardsSolution probable_improvement;
    private String status;
}
