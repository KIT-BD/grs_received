package com.grs.api.mobileApp.controller;

import com.grs.api.mobileApp.dto.MobileResponse;
import com.grs.api.mobileApp.service.MobileGeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MobileGeoController {

    private final MobileGeoService mobileGeoService;
    @GetMapping("/doptor/data")
    public MobileResponse getDoptorData(
            @RequestParam(value = "api_url") String apiUrl,
            @RequestParam(value = "api_type", required = false, defaultValue = "GET") String apiType,
            @RequestParam(value = "prams", required = false) String prams
    ) {

        Integer code = prams != null ? Integer.valueOf(prams.split("=")[1]): null;

        List<?> getAll = mobileGeoService.findGeo(apiUrl,code);

        return MobileResponse.builder()
                .status("success")
                .data(getAll)
                .build();
    }
}
