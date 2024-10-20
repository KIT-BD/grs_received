package com.grs.api.mobileApp.controller;

import com.grs.api.mobileApp.dto.*;
import com.grs.api.mobileApp.service.MobilePublicAPIService;
import com.grs.api.model.response.EducationDTO;
import com.grs.api.model.response.OccupationDTO;
import com.grs.core.domain.grs.CountryInfo;
import com.grs.core.domain.grs.Education;
import com.grs.core.domain.grs.Occupation;
import com.grs.core.domain.grs.SpProgramme;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class MobilePublicAPIController {

    private final MobilePublicAPIService mobilePublicAPIService;
    @GetMapping("/occupation/list")
    public MobileResponse getOccupationsList(){
        List<Occupation> findAll = mobilePublicAPIService.getOccupationList();
        List<MobileOccupationDTO> dtoList = new ArrayList<>();

        for (Occupation o: findAll){
            MobileOccupationDTO converted = MobileOccupationDTO.builder()
                    .id(o.getId())
                    .occupation_eng(o.getOccupationEnglish())
                    .occupation_bng(o.getOccupationBangla())
                    .status(o.getStatus() != null && o.getStatus() ? 1 : 0)
                    .build();
            dtoList.add(converted);
        }
        return MobileResponse.builder()
                .status("success")
                .data(dtoList)
                .build();
    }

    @GetMapping("/qualification/list")
    public MobileResponse getQualificationList(){
        List<Education> findAll = mobilePublicAPIService.getQualificationList();
        List<MobileEducationDTO> dtoList = new ArrayList<>();

        for (Education e: findAll){
            MobileEducationDTO converted = MobileEducationDTO.builder()
                    .id(e.getId())
                    .education_bng(e.getEducationBangla())
                    .education_eng(e.getEducationEnglish())
                    .status(e.getStatus() != null && e.getStatus() ? 1 : 0)
                    .build();
            dtoList.add(converted);
        }
        return MobileResponse.builder()
                .status("success")
                .data(dtoList)
                .build();
    }

    @GetMapping("/nationality/list")
    public MobileResponse getNationalityList(){
        List<CountryInfo> findAll = mobilePublicAPIService.getNationalityList();
        List<MobileCountryInfoDTO> dtoList = new ArrayList<>();

        for (CountryInfo c: findAll){
            MobileCountryInfoDTO converted = MobileCountryInfoDTO.builder()
                    .id(c.getId())
                    .country_name_eng(c.getCountryNameEng())
                    .country_name_bng(c.getCountryNameBng())
                    .nationality_eng(c.getNationalityEng())
                    .nationality_bng(c.getNationalityBng())
                    .build();
            dtoList.add(converted);
        }
        return MobileResponse.builder()
                .status("success")
                .data(dtoList)
                .build();
    }

    @GetMapping("/sp-programme/list")
    public MobileResponse getSpProgrammeList(){
        List<SpProgramme> findAll = mobilePublicAPIService.getSpProgrammeList();
        List<MobileSpProgrammeDTO> dtoList = new ArrayList<>();

        for (SpProgramme s: findAll){
            MobileSpProgrammeDTO converted = MobileSpProgrammeDTO.builder()
                    .id(s.getId())
                    .name_en(s.getNameEn())
                    .name_bn(s.getNameBn())
                    .office_id(s.getOfficeId())
                    .status(s.getStatus() != null && s.getStatus() ? 1 : 0)
                    .build();
            dtoList.add(converted);
        }
        return MobileResponse.builder()
                .status("success")
                .data(dtoList)
                .build();
    }
}
