package com.grs.mobileApp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MobileResponse {
    public String status;
    public Object data;
}
