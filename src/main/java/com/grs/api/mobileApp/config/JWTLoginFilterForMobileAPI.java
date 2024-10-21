package com.grs.api.mobileApp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grs.api.config.security.CustomAuthenticationToken;
import com.grs.api.config.security.UserDetailsImpl;
import com.grs.api.mobileApp.dto.DataDTO;
import com.grs.api.mobileApp.dto.MobileAuthDTO;
import com.grs.api.mobileApp.dto.MobileResponse;
import com.grs.api.model.UserInformation;
import com.grs.api.model.response.ErrorDTO;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.service.ComplainantService;
import javassist.NotFoundException;
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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
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

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException {

        String username = req.getParameter(USERNAME_REQUEST_PARAM);
        String password = req.getParameter(PASSWORD_REQUEST_PARAM);

        return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password,
                        Collections.emptyList()
                )
        );
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
        if (complainant == null) {
            try {
                throw new NotFoundException("Complainant not found");
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        DataDTO responseDTO = DataDTO.builder()
                .user_info(
                        MobileAuthDTO.builder()
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
                                .nationality_id(Optional.ofNullable(complainant.getCountryInfo()).map(country -> country.getId()).orElse(null))
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
                                .build()

                )
                .token(JWT)
                .build();

        MobileResponse mobileResponse = MobileResponse.builder()
                .status("success")
                .data(Collections.singletonList(responseDTO))
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
                                              AuthenticationException failed) throws IOException, ServletException {

        String[] message = failed.getMessage().split(" ");
        String lastMessage = message[message.length - 1];

        ErrorDTO error  = ErrorDTO.builder()
                .message("")
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        if (lastMessage.compareTo("credentials") == 0) {
            error.setMessage("Username or Password is incorrect");
        } else if (lastMessage.compareTo("disabled") == 0) {
            error.setMessage("User is disabled");
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.addHeader("content-type", "application/json;charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), error);
    }
}
