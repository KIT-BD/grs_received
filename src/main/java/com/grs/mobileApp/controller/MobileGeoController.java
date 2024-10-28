package com.grs.mobileApp.controller;

import com.grs.mobileApp.dto.MobileResponse;
import com.grs.mobileApp.service.MobileGeoService;
import com.grs.core.domain.projapoti.District;
import com.grs.core.domain.projapoti.Division;
import com.grs.core.domain.projapoti.Upazila;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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

        List<?> getAll = mobileGeoService.findGeo(apiUrl, code);

        List<Map<String, Object>> responseList = new ArrayList<>();

        for (Object var : getAll) {
            Map<String, Object> response = new LinkedHashMap<>();
            if (var instanceof Division) {
                Division division = (Division) var;
                response.put("bbsCode", null);
                response.put("name", division.getNameEnglish());
                response.put("nameBn", division.getNameBangla());
                response.put("id", division.getId());

            } else if (var instanceof District) {
                District district = (District) var;
                response.put("bbsCode", null);
                response.put("division", district.getDivisionId());
                response.put("name", district.getNameEnglish());
                response.put("nameBn", district.getNameBangla());
                response.put("id", district.getId());
            } else if (var instanceof Upazila){
                Upazila upazila = (Upazila) var;
                response.put("bbsCode", null);
                response.put("division",upazila.getDivisionId());
                response.put("district", upazila.getDistrictId());
                response.put("name", upazila.getNameEnglish());
                response.put("nameBn", upazila.getNameBangla());
                response.put("id", upazila.getId());
            }
            responseList.add(response);
        }


        return MobileResponse.builder()
                .status("success")
                .data(responseList)
                .build();
    }
}
