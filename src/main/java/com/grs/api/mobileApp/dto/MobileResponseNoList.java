package com.grs.api.mobileApp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MobileResponseNoList {
    public String status;
    public Object data;
}
