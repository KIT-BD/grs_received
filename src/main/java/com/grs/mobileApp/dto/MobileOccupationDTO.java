package com.grs.mobileApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileOccupationDTO {
    private Long id;
    private String occupation_bng;
    private String occupation_eng;
    private Integer status;
}
