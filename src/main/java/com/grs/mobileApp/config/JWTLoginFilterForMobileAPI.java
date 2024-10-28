package com.grs.mobileApp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grs.api.config.security.CustomAuthenticationToken;
import com.grs.api.config.security.UserDetailsImpl;
import com.grs.api.model.UserInformation;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.CountryInfo;
import com.grs.core.service.ComplainantService;
import com.grs.mobileApp.dto.DataDTO;
import com.grs.mobileApp.dto.LoginDTO;
import com.grs.mobileApp.dto.MobileAuthDTO;
import com.grs.mobileApp.dto.MobileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.grs.api.config.security.TokenAuthenticationServiceUtil.constuctJwtToken;
import static com.grs.utils.Constant.HEADER_STRING;

public class JWTLoginFilterForMobileAPI extends AbstractAuthenticationProcessingFilter {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final ComplainantService complainantService;
    private final String USERNAME_REQUEST_PARAM = "username";
    private final String PASSWORD_REQUEST_PARAM = "password";

    public JWTLoginFilterForMobileAPI(String url, AuthenticationManager authManager, BCryptPasswordEncoder bCryptPasswordEncoder, ComplainantService complainantService) {
        super(new AntPathRequestMatcher(url, "POST"));
        setAuthenticationManager(authManager);
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.complainantService = complainantService;
    }

//    @Override
//    public Authentication attemptAuthentication(
//            HttpServletRequest req, HttpServletResponse res)
//            throws AuthenticationException {
//
//        String username = req.getParameter(USERNAME_REQUEST_PARAM);
//        String password = req.getParameter(PASSWORD_REQUEST_PARAM);
//
//        return getAuthenticationManager().authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        username,
//                        password,
//                        Collections.emptyList()
//                )
//        );
//    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            LoginDTO authRequest = objectMapper.readValue(req.getInputStream(), LoginDTO.class);

            String username = authRequest.getUsername();
            String password = authRequest.getPassword();

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            password,
                            Collections.emptyList()
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException("Error reading request body", e);
        }
    }


    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response, FilterChain chain,
            Authentication authentication) throws IOException {

        UserInformation userInformation;
        String name;
        Set<String> permissionNamesSet;
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            name = authentication.getName();
            permissionNamesSet = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            userInformation = userDetails.getUserInformation();

        } catch (Exception e) {
            CustomAuthenticationToken token = (CustomAuthenticationToken) authentication;
            name = token.getName();
            permissionNamesSet = token.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            userInformation = token.getUserInformation();
        }

        String JWT = constuctJwtToken(name, permissionNamesSet, userInformation);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), JWT, authentication.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        Complainant complainant = complainantService.findOne(userInformation.getUserId());

        DataDTO responseDTO = DataDTO.builder()
                .user_info(
                        MobileAuthDTO.builder()
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
                                .build()

                )
                .token(JWT)
                .build();

        MobileResponse mobileResponse = MobileResponse.builder()
                .status("success")
                .data(responseDTO)
                .build();

        response.addHeader(HEADER_STRING,  JWT);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ObjectMapper mapper = new ObjectMapper();
        response.addHeader("content-type", "application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(), mobileResponse);
    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

//        String message = failed.getMessage();
//
//        if (message == null) {
//            message = "Authentication failed due to an unknown error";
//        }

        MobileResponse error  = MobileResponse.builder()
                .status("error")
                .data("Wrong username or password")
                .build();

//        if (message.toLowerCase().contains("bad credentials")) {
//            error.setMessage("Username or Password is incorrect");
//        } else if (message.toLowerCase().contains("disabled")) {
//            error.setMessage("User is disabled");
//        } else {
//            error.setMessage("Authentication failed");
//        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.addHeader("content-type", "application/json;charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), error);
    }
}
