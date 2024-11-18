package com.grs.mobileApp.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grs.api.config.security.*;
import com.grs.api.model.UserInformation;
import com.grs.api.sso.LoginRequest;
import com.grs.core.dao.GrsRoleDAO;
import com.grs.core.dao.UserDAO;
import com.grs.core.domain.doptor.*;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.CountryInfo;
import com.grs.core.domain.grs.GrsRole;
import com.grs.core.service.ComplainantService;
import com.grs.mobileApp.dto.*;
import com.grs.utils.BanglaConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.grs.api.config.security.TokenAuthenticationServiceUtil.constuctJwtToken;
import static com.grs.utils.Constant.HEADER_STRING;

@Slf4j
public class JWTAdminLoginFilterForMobileAPI extends AbstractAuthenticationProcessingFilter {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final String USERNAME_REQUEST_PARAM = "username";
    private final String PASSWORD_REQUEST_PARAM = "password";

    private final OISFUserDetailsServiceImpl oisfUserDetailsService;

    private final GrsRoleDAO grsRoleDAO;

    @Value("${nothi.mobileApp.url}")
    private String nothiMobileUserVerifyUrl;


    public JWTAdminLoginFilterForMobileAPI(String url, AuthenticationManager authManager, BCryptPasswordEncoder bCryptPasswordEncoder, OISFUserDetailsServiceImpl oisfUserDetailsService, GrsRoleDAO grsRoleDAO) {
        super(new AntPathRequestMatcher(url, "POST"));
        this.grsRoleDAO = grsRoleDAO;
        setAuthenticationManager(authManager);
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.oisfUserDetailsService = oisfUserDetailsService;
    }
    @Getter
    @Setter
    private static ResponseEntity<String> responseBody;

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            LoginRequest authRequest = objectMapper.readValue(req.getInputStream(), LoginRequest.class);

            String username = authRequest.getUsername();
            String password = authRequest.getPassword();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("api-version", "1");

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("username", username);
            formData.add("password", password);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters()
                    .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    nothiMobileUserVerifyUrl,
                    requestEntity,
                    String.class
            );
            if (responseEntity == null){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        password,
                        Collections.emptyList()
                );

                setResponseBody(null);
                return authToken;
            }

            else if (responseEntity.getStatusCode() == HttpStatus.OK) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        password,
                        Collections.emptyList()
                );

                setResponseBody(responseEntity);
                return authToken;
            } else {
                throw new BadCredentialsException("Authentication failed");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading request body", e);
        } catch (Exception e) {
            throw new AuthenticationServiceException("Authentication failed", e);
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response, FilterChain chain,
            Authentication authentication) throws IOException {
        
//        String username = authentication.getName();
//        Object principal = authentication.getPrincipal();

//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        Set<String> permissionNamesSet = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        
        ResponseEntity<String> responseEntity = getResponseBody();
        String responseBody = responseEntity.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MobileAdminLoginResponse responseMap = objectMapper.readValue(responseBody, MobileAdminLoginResponse.class);

        User user = responseMap.getData().getUser();
//        List<OfficeInfo> officeInfos = responseMap.getUser_info().getOffice_info();
//        EmployeeInfo employeeInfo = responseMap.getUser_info().getEmployee_info();
//        HashMap<String, OfficeOrganogram> organogramInfo = responseMap.getUser_info().getOrganogram_info();

        UserInformation userInformation = this.oisfUserDetailsService.getUserInfo(responseMap.getData());
        String roleName = null;
        if (userInformation.getGrsUserType() != null) {
            roleName = userInformation.getGrsUserType().name();
        } else {
            roleName = userInformation.getOisfUserType().name();
        }
        GrsRole grsRole = this.grsRoleDAO.findByRole(roleName);
        List<GrantedAuthorityImpl> grantedAuthorities = grsRole
                .getPermissions()
                .stream()
                .map(permission -> {
                    return GrantedAuthorityImpl.builder()
                            .role(permission.getName())
                            .build();
                }).collect(Collectors.toList());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .username(user.getUsername())
                .isAccountAuthenticated(true)
                .grantedAuthorities(grantedAuthorities).userInformation(userInformation).build();


        try {
            ObjectMapper responseMapper = new ObjectMapper();
            Map<String, Object> responseData = responseMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

            Map<String,Object> data = new HashMap<>();
            data.put("user_info",responseData.get("data"));
            data.put("user_type",userInformation.getOisfUserType());
//            data.put("token", TokenAuthenticationServiceUtil.addAuthenticationForMyGovMobile(userDetails, request, response));
            data.put("token", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyMDAwMDAwMDA1NTgiLCJwZXJtaXNzaW9ucyI6WyJBRERfU0VSVklDRVMiLCJFRElUX0NJVElaRU5fQ0hBUlRFUiIsIk9GRkxJTkVfR1JJRVZBTkNFX1VQTE9BRCIsIkVESVRfU0VSVklDRVMiLCJBRERfUFVCTElDX0dSSUVWQU5DRVMiLCJBRERfT0ZGSUNJQUxfR1JJRVZBTkNFUyIsIlZJRVdfU1VHR0VTVElPTiIsIlZJRVdfUFVCTElDX0dSSUVWQU5DRVMiLCJWSUVXX1JFR0lTVEVSIiwiVklFV19TVEFGRl9HUklFVkFOQ0VTIiwiVklFV19PRkZJQ0lBTF9HUklFVkFOQ0VTIiwiQUREX1NUQUZGX0dSSUVWQU5DRVMiLCJWSUVXX0NJVElaRU5fQ0hBUlRFUiIsIlZJRVdfU0VSVklDRVMiLCJBRERfQ0lUSVpFTl9DSEFSVEVSIl0sInVzZXJfaW5mbyI6eyJ1c2VySWQiOjgsInVzZXJuYW1lIjoiMjAwMDAwMDAwNTU4IiwidXNlclR5cGUiOiJPSVNGX1VTRVIiLCJvaXNmVXNlclR5cGUiOiJHUk8iLCJncnNVc2VyVHlwZSI6bnVsbCwib2ZmaWNlSW5mb3JtYXRpb24iOnsib2ZmaWNlSWQiOjI4LCJvZmZpY2VOYW1lQmFuZ2xhIjoi4Kau4Kao4KeN4Kak4KeN4Kaw4Ka_4Kaq4Kaw4Ka_4Ka34KamIOCmrOCmv-CmreCmvuCmlyAiLCJvZmZpY2VOYW1lRW5nbGlzaCI6Ik1pbmlzdHJ5IERpdmlzaW9uIiwib2ZmaWNlTWluaXN0cnlJZCI6NCwib2ZmaWNlT3JpZ2luSWQiOjQyLCJuYW1lIjoi4Kau4KeL4KaDIOCmtuCmvuCmq-CmvuCnn-CmvuCmpCDgpq7gpr7gprngpqzgp4Hgpqwg4Kaa4KeM4Kan4KeB4Kaw4KeAIiwiZGVzaWduYXRpb24iOiLgpq_gp4Hgppfgp43gpq7gprjgpprgpr_gpqwgKOCmuOCmguCmr-CngeCmleCnjeCmpCkiLCJlbXBsb3llZVJlY29yZElkIjo3ODMwMiwib2ZmaWNlVW5pdE9yZ2Fub2dyYW1JZCI6MTA3ODYsImxheWVyTGV2ZWwiOjEsImdlb0RpdmlzaW9uSWQiOjMsImdlb0Rpc3RyaWN0SWQiOjE4fSwiaXNBcHBlYWxPZmZpY2VyIjp0cnVlLCJpc09mZmljZUFkbWluIjpmYWxzZSwiaXNDZW50cmFsRGFzaGJvYXJkVXNlciI6dHJ1ZSwiaXNDZWxsR1JPIjp0cnVlLCJpc01vYmlsZUxvZ2luIjpmYWxzZSwiaXNNeUdvdkxvZ2luIjpudWxsLCJ0b2tlbiI6bnVsbH19.tUtVEIT38NGGP3tYTTrJdjMEHd-_uLJsZfPxLNGCu4PcR4YjSqhQSPnrh75bRaHllT-PHWII0LM_ODwSaehUWw");

            Map<String,Object> mobileResponse = new HashMap<>();
            mobileResponse.put("status","success");
            mobileResponse.put("data", data);

            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), mobileResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        if (getResponseBody() == null){
            Map<String,Object> mobileResponse = new HashMap<>();
            mobileResponse.put("status","error");
            mobileResponse.put("data", "n-doptor is not responding");

            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), mobileResponse);
            return;
        }

        MobileResponse error = MobileResponse.builder()
                .status("error")
                .data("Wrong username or password")
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.addHeader("content-type", "application/json;charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), error);
    }

}
