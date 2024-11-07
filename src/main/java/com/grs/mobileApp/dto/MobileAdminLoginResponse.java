package com.grs.mobileApp.dto;

import com.grs.core.domain.doptor.UserInfo;
import lombok.Data;

@Data
public class MobileAdminLoginResponse {
    private String status;
    private UserInfo data;
}
