package com.grs.api.mobileApp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MobileResponse {
    public String status;
    public List<String> data;
}
