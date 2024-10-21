package com.grs.api.mobileApp.controller;

import com.grs.api.mobileApp.dto.MobileAuthDTO;
import com.grs.api.mobileApp.dto.MobileResponse;
import com.grs.api.mobileApp.dto.MobileResponseNoList;
import com.grs.api.mobileApp.service.MobileAuthService;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.CountryInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mobile")
public class MobileAuthController {

    private final MobileAuthService mobileAuthService;

    @GetMapping("/complainant/show")
    public MobileResponseNoList checkUser(
            @RequestParam("mobile_number") String mobileNumber
    ){
        Complainant complainant = mobileAuthService.findByMobileNumber(mobileNumber);
        MobileAuthDTO responseDTO = MobileAuthDTO.builder()
                .id(complainant.getId())
                .name(complainant.getUsername())
                .identification_value(Optional.ofNullable(complainant.getIdentificationValue()).orElse(null))
                .identification_type(Optional.ofNullable(complainant.getIdentificationType()).map(String::valueOf).orElse(null))
                .mobile_number(Optional.ofNullable(complainant.getPhoneNumber()).orElse(null))
                .email(Optional.ofNullable(complainant.getEmail()).orElse(null))
                .birth_date(Optional.ofNullable(complainant.getBirthDate()).map(String::valueOf).orElse(null))
                .occupation(Optional.ofNullable(complainant.getOccupation()).orElse(null))
                .educational_qualification(Optional.ofNullable(complainant.getEducation()).orElse(null))
                .gender(Optional.ofNullable(complainant.getGender()).map(String::valueOf).orElse(null))
                .username(Optional.ofNullable(complainant.getUsername()).orElse(null))
                .nationality_id(Optional.ofNullable(complainant.getCountryInfo()).map(CountryInfo::getId).orElse(null))
                .present_address_street(Optional.ofNullable(complainant.getPresentAddressStreet()).orElse(null))
                .present_address_house(Optional.ofNullable(complainant.getPresentAddressHouse()).orElse(null))
                .present_address_division_id(Optional.ofNullable(complainant.getPresentAddressDivisionId()).map(Long::valueOf).orElse(null))
                .present_address_division_name_bng(Optional.ofNullable(complainant.getPresentAddressDivisionNameBng()).orElse(null))
                .present_address_division_name_eng(Optional.ofNullable(complainant.getPresentAddressDivisionNameEng()).orElse(null))
                .present_address_district_id(Optional.ofNullable(complainant.getPresentAddressDistrictId()).map(Long::valueOf).orElse(null))
                .present_address_district_name_bng(Optional.ofNullable(complainant.getPresentAddressDistrictNameBng()).orElse(null))
                .present_address_district_name_eng(Optional.ofNullable(complainant.getPresentAddressDistrictNameEng()).orElse(null))
                .present_address_type_id(Optional.ofNullable(complainant.getPresentAddressTypeId()).map(Long::valueOf).orElse(null))
                .present_address_type_name_bng(Optional.ofNullable(complainant.getPresentAddressTypeNameBng()).orElse(null))
                .present_address_type_name_eng(Optional.ofNullable(complainant.getPresentAddressTypeNameEng()).orElse(null))
                .present_address_type_value(Optional.ofNullable(complainant.getPresentAddressTypeValue()).map(String::valueOf).orElse(null))
                .present_address_postal_code(Optional.ofNullable(complainant.getPresentAddressPostalCode()).orElse(null))
                .is_blacklisted(false)
                .permanent_address_street(Optional.ofNullable(complainant.getPermanentAddressStreet()).orElse(null))
                .permanent_address_house(Optional.ofNullable(complainant.getPermanentAddressHouse()).orElse(null))
                .permanent_address_division_id(Optional.ofNullable(complainant.getPermanentAddressDivisionId()).map(Long::valueOf).orElse(null))
                .permanent_address_division_name_bng(Optional.ofNullable(complainant.getPermanentAddressDivisionNameBng()).orElse(null))
                .permanent_address_division_name_eng(Optional.ofNullable(complainant.getPermanentAddressDivisionNameEng()).orElse(null))
                .permanent_address_district_id(Optional.ofNullable(complainant.getPermanentAddressDistrictId()).map(Long::valueOf).orElse(null))
                .permanent_address_district_name_bng(Optional.ofNullable(complainant.getPermanentAddressDistrictNameBng()).orElse(null))
                .permanent_address_district_name_eng(Optional.ofNullable(complainant.getPermanentAddressDistrictNameEng()).orElse(null))
                .permanent_address_type_id(Optional.ofNullable(complainant.getPermanentAddressTypeId()).map(Long::valueOf).orElse(null))
                .permanent_address_type_name_bng(Optional.ofNullable(complainant.getPermanentAddressTypeNameBng()).orElse(null))
                .permanent_address_type_name_eng(Optional.ofNullable(complainant.getPermanentAddressTypeNameEng()).orElse(null))
                .permanent_address_type_value(Optional.ofNullable(complainant.getPermanentAddressTypeValue()).map(String::valueOf).orElse(null))
                .permanent_address_postal_code(Optional.ofNullable(complainant.getPermanentAddressPostalCode()).orElse(null))
                .foreign_permanent_address_zipcode(complainant.getForeignPermanentAddressZipCode())
                .foreign_permanent_address_state(Optional.ofNullable(complainant.getForeignPermanentAddressState()).orElse(null))
                .foreign_permanent_address_city(Optional.ofNullable(complainant.getForeignPermanentAddressCity()).orElse(null))
                .foreign_permanent_address_line2(Optional.ofNullable(complainant.getForeignPermanentAddressLine2()).orElse(null))
                .foreign_permanent_address_line1(Optional.ofNullable(complainant.getForeignPermanentAddressLine1()).orElse(null))
                .foreign_present_address_zipcode(Optional.ofNullable(complainant.getForeignPresentAddressZipCode()).orElse(null))
                .foreign_present_address_state(Optional.ofNullable(complainant.getForeignPresentAddressState()).orElse(null))
                .foreign_present_address_city(Optional.ofNullable(complainant.getForeignPresentAddressCity()).orElse(null))
                .foreign_present_address_line2(Optional.ofNullable(complainant.getForeignPresentAddressLine2()).orElse(null))
                .foreign_present_address_line1(Optional.ofNullable(complainant.getForeignPresentAddressLine1()).orElse(null))
                .is_authenticated(complainant.isAuthenticated())
                .created_at(Optional.ofNullable(complainant.getCreatedAt()).map(String::valueOf).orElse(null))
                .modified_at(null)
                .created_by(Optional.ofNullable(complainant.getCreatedBy()).map(String::valueOf).orElse(null))
                .modified_by(Optional.ofNullable(complainant.getModifiedBy()).map(String::valueOf).orElse(null))
                .status(Optional.ofNullable(complainant.getStatus()).map(String::valueOf).orElse(null))
                .present_address_country_id(Optional.ofNullable(complainant.getPresentAddressCountryId()).orElse(null))
                .permanent_address_country_id(Optional.ofNullable(complainant.getPermanentAddressCountryId()).orElse(null))
                .blacklister_office_id(null)
                .blacklister_office_name(null)
                .blacklist_reason(null)
                .is_requested(null)
                .build();

        return MobileResponseNoList.builder()
                .status("success")
                .data(responseDTO)
                .build();
    }
}
