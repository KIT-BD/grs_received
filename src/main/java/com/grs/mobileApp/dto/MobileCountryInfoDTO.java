package com.grs.mobileApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MobileCountryInfoDTO {
    private Long id;
    private String country_name_eng;
    private String nationality_eng;
    private String country_name_bng;
    private String nationality_bng;
}
