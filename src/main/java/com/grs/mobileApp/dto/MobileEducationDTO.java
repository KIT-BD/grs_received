package com.grs.mobileApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class MobileEducationDTO {
    private Long id;
    private String education_bng;
    private String education_eng;
    private Integer status;
}
