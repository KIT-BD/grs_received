package com.grs.mobileApp.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DataDTO {
    private MobileAuthDTO user_info;
    private String token;
}
