package com.grs.api.mobileApp.controller;

import com.grs.api.mobileApp.dto.MobileAuthDTO;
import com.grs.api.mobileApp.dto.MobileResponse;
import com.grs.api.mobileApp.dto.MobileResponseNoList;
import com.grs.api.mobileApp.service.MobileAuthService;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.CountryInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

        if (complainant == null) {
            return MobileResponseNoList.builder()
                            .status("error")
                            .data("User not found for the mobile number: " + mobileNumber)
                            .build();
        }

        MobileAuthDTO responseDTO = MobileAuthDTO.builder()
                .id(complainant.getId())
                .name(complainant.getUsername())
                .identification_value(complainant.getIdentificationValue())
                .identification_type(Optional.ofNullable(complainant.getIdentificationType()).map(String::valueOf).orElse(null))
                .mobile_number(complainant.getPhoneNumber())
                .email(complainant.getEmail())
                .birth_date(Optional.ofNullable(complainant.getBirthDate()).map(String::valueOf).orElse(null))
                .occupation(complainant.getOccupation())
                .educational_qualification(complainant.getEducation())
                .gender(Optional.ofNullable(complainant.getGender()).map(String::valueOf).orElse(null))
                .username(complainant.getUsername())
                .nationality_id(Optional.ofNullable(complainant.getCountryInfo()).map(CountryInfo::getId).orElse(null))
                .present_address_street(complainant.getPresentAddressStreet())
                .present_address_house(complainant.getPresentAddressHouse())
                .present_address_division_id(Optional.ofNullable(complainant.getPresentAddressDivisionId()).map(Long::valueOf).orElse(null))
                .present_address_division_name_bng(complainant.getPresentAddressDivisionNameBng())
                .present_address_division_name_eng(complainant.getPresentAddressDivisionNameEng())
                .present_address_district_id(Optional.ofNullable(complainant.getPresentAddressDistrictId()).map(Long::valueOf).orElse(null))
                .present_address_district_name_bng(complainant.getPresentAddressDistrictNameBng())
                .present_address_district_name_eng(complainant.getPresentAddressDistrictNameEng())
                .present_address_type_id(Optional.ofNullable(complainant.getPresentAddressTypeId()).map(Long::valueOf).orElse(null))
                .present_address_type_name_bng(complainant.getPresentAddressTypeNameBng())
                .present_address_type_name_eng(complainant.getPresentAddressTypeNameEng())
                .present_address_type_value(Optional.ofNullable(complainant.getPresentAddressTypeValue()).map(String::valueOf).orElse(null))
                .present_address_postal_code(complainant.getPresentAddressPostalCode())
                .is_blacklisted(false)
                .permanent_address_street(complainant.getPermanentAddressStreet())
                .permanent_address_house(complainant.getPermanentAddressHouse())
                .permanent_address_division_id(Optional.ofNullable(complainant.getPermanentAddressDivisionId()).map(Long::valueOf).orElse(null))
                .permanent_address_division_name_bng(complainant.getPermanentAddressDivisionNameBng())
                .permanent_address_division_name_eng(complainant.getPermanentAddressDivisionNameEng())
                .permanent_address_district_id(Optional.ofNullable(complainant.getPermanentAddressDistrictId()).map(Long::valueOf).orElse(null))
                .permanent_address_district_name_bng(complainant.getPermanentAddressDistrictNameBng())
                .permanent_address_district_name_eng(complainant.getPermanentAddressDistrictNameEng())
                .permanent_address_type_id(Optional.ofNullable(complainant.getPermanentAddressTypeId()).map(Long::valueOf).orElse(null))
                .permanent_address_type_name_bng(complainant.getPermanentAddressTypeNameBng())
                .permanent_address_type_name_eng(complainant.getPermanentAddressTypeNameEng())
                .permanent_address_type_value(Optional.ofNullable(complainant.getPermanentAddressTypeValue()).map(String::valueOf).orElse(null))
                .permanent_address_postal_code(complainant.getPermanentAddressPostalCode())
                .foreign_permanent_address_zipcode(complainant.getForeignPermanentAddressZipCode())
                .foreign_permanent_address_state(complainant.getForeignPermanentAddressState())
                .foreign_permanent_address_city(complainant.getForeignPermanentAddressCity())
                .foreign_permanent_address_line2(complainant.getForeignPermanentAddressLine2())
                .foreign_permanent_address_line1(complainant.getForeignPermanentAddressLine1())
                .foreign_present_address_zipcode(complainant.getForeignPresentAddressZipCode())
                .foreign_present_address_state(complainant.getForeignPresentAddressState())
                .foreign_present_address_city(complainant.getForeignPresentAddressCity())
                .foreign_present_address_line2(complainant.getForeignPresentAddressLine2())
                .foreign_present_address_line1(complainant.getForeignPresentAddressLine1())
                .is_authenticated(complainant.isAuthenticated())
                .created_at(Optional.ofNullable(complainant.getCreatedAt()).map(String::valueOf).orElse(null))
                .modified_at(null)
                .created_by(Optional.ofNullable(complainant.getCreatedBy()).map(String::valueOf).orElse(null))
                .modified_by(Optional.ofNullable(complainant.getModifiedBy()).map(String::valueOf).orElse(null))
                .status(Optional.ofNullable(complainant.getStatus()).map(String::valueOf).orElse(null))
                .present_address_country_id(complainant.getPresentAddressCountryId())
                .permanent_address_country_id(complainant.getPermanentAddressCountryId())
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
