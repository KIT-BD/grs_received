package com.grs.api.mobileApp.controller;

import com.grs.api.mobileApp.dto.MobileAuthDTO;
import com.grs.api.mobileApp.dto.MobileResponse;
import com.grs.api.mobileApp.dto.MobileResponseNoList;
import com.grs.api.mobileApp.service.MobileAuthService;
import com.grs.api.mobileApp.service.MobilePublicAPIService;
import com.grs.api.model.request.ComplainantDTO;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.CountryInfo;
import com.grs.core.domain.grs.Occupation;
import com.grs.core.service.ComplainantService;
import com.grs.core.service.OccupationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/complainant")
public class MobileAuthController {

    private final MobileAuthService mobileAuthService;
    private final ComplainantService complainantService;
    private final OccupationService occupationService;

    @PostMapping("/save")
    public MobileResponseNoList registerComplainant(@RequestBody MobileAuthDTO mobileAuthDTO){

        Long occupationId = Long.valueOf(mobileAuthDTO.getOccupation());

        if (mobileAuthDTO.getGender() == null || mobileAuthDTO.getGender().trim().isEmpty()){
            return MobileResponseNoList.builder()
                    .status("error")
                    .data("Gender is required")
                    .build();
        }
        if (complainantService.findComplainantByPhoneNumber(mobileAuthDTO.getMobile_number()) != null){
            return MobileResponseNoList.builder()
                    .status("error")
                    .data("Complainant already exists")
                    .build();
        }

        ComplainantDTO complainantDTO = ComplainantDTO.builder()
                .name(mobileAuthDTO.getName())
                .identificationValue(mobileAuthDTO.getIdentification_value())
                .identificationType(mobileAuthDTO.getIdentification_type())
                .phoneNumber(mobileAuthDTO.getMobile_number())
                .email(mobileAuthDTO.getEmail())
                .birthDate(mobileAuthDTO.getBirth_date())
                .occupation(occupationService.getOccupation(occupationId).getOccupationEnglish())
                .gender(mobileAuthDTO.getGender())
                .nationality(String.valueOf(mobileAuthDTO.getNationality_id()))
                .permanentAddressStreet(mobileAuthDTO.getPermanent_address_street())
                .permanentAddressHouse(mobileAuthDTO.getPermanent_address_house())
                .permanentAddressCountryId(String.valueOf(mobileAuthDTO.getPermanent_address_country_id()))
                .build();

        Complainant complainant = complainantService.insertComplainant(complainantDTO);

        MobileAuthDTO responseDTO = MobileAuthDTO.builder()
                .id(complainant.getId())
                .name(complainant.getUsername())
                .identification_value(complainant.getIdentificationValue())
                .identification_type(Optional.ofNullable(complainant.getIdentificationType()).map(String::valueOf).orElse(null))
                .mobile_number(complainant.getPhoneNumber())
                .email(complainant.getEmail())
                .birth_date(Optional.ofNullable(complainant.getBirthDate()).map(String::valueOf).orElse(null))
                .occupation(String.valueOf(occupationId))
                .educational_qualification(complainant.getEducation())
                .gender(Optional.ofNullable(complainant.getGender()).map(String::valueOf).orElse(null))
                .username(complainant.getUsername())
                .nationality_id(Optional.ofNullable(complainant.getCountryInfo()).map(CountryInfo::getId).orElse(null))
                .present_address_street(complainant.getPresentAddressStreet())
                .present_address_house(complainant.getPresentAddressHouse())
                .permanent_address_country_id(complainant.getPermanentAddressCountryId())
                .is_authenticated(complainant.isAuthenticated() ? 1L : 0L)
                .created_at(Optional.ofNullable(complainant.getCreatedAt()).map(String::valueOf).orElse(null))
                .modified_at(String.valueOf(complainant.getCreatedAt()))
                .build();

        return MobileResponseNoList.builder()
                .status("success")
                .data(responseDTO)
                .build();
    }

    @GetMapping("/show")
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
                .name(complainant.getName())
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
                .is_authenticated(complainant.isAuthenticated() ? 1L : 0L)
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
