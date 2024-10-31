package com.grs.mobileApp.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grs.api.config.security.CustomAuthenticationToken;
import com.grs.api.config.security.GrantedAuthorityImpl;
import com.grs.api.config.security.OISFUserDetailsServiceImpl;
import com.grs.api.config.security.UserDetailsImpl;
import com.grs.api.model.UserInformation;
import com.grs.api.sso.LoginRequest;
import com.grs.core.dao.GrsRoleDAO;
import com.grs.core.dao.UserDAO;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.CountryInfo;
import com.grs.core.domain.grs.GrsRole;
import com.grs.core.domain.projapoti.User;
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

    public JWTAdminLoginFilterForMobileAPI(String url, AuthenticationManager authManager, BCryptPasswordEncoder bCryptPasswordEncoder) {
        super(new AntPathRequestMatcher(url, "POST"));
        setAuthenticationManager(authManager);
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
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
                    "https://api-stage.doptor.gov.bd/api/user/verify",
                    requestEntity,
                    String.class
            );

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        password,
                        Collections.emptyList()
                );

                setResponseBody(responseEntity);

                log.info(username + password + authToken);

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

    private Authentication doAuthentication(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        User user = this.userDAO.findByUsername(BanglaConverter.convertToEnglish(name));

        if (user != null) {
            UserInformation userInformation = this.oisfUserDetailsService.getUserInfo(user);
            String roleName = null;
            if(userInformation.getGrsUserType() != null) {
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
            return new CustomAuthenticationToken(name, password, grantedAuthorities, userInformation);
        } else {
            return null;
        }
    }


    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response, FilterChain chain,
            Authentication authentication) throws IOException {

        String username = authentication.getName();
        Object principal = authentication.getPrincipal();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> permissionNamesSet = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        String JWT = constuctJwtToken(username, permissionNamesSet, null);

        ResponseEntity<String> responseEntity = getResponseBody();
        String responseBody = responseEntity.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

        Map<String, Object> userInfo = Optional.ofNullable(responseMap)
                .map(map -> (Map<String, Object>) map.get("data"))
                .orElse(Collections.emptyMap());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Map<String,Object> data = new HashMap<>();
        data.put("user_info",userInfo);
        data.put("l","gro");
        data.put("token", JWT);


        Map<String,Object> mobileResponse = new HashMap<>();
        mobileResponse.put("status","success");
        mobileResponse.put("data", data);

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), mobileResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

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
