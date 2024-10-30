package com.grs.mobileApp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.grs.api.sso.LoginRequest;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RestController
public class MobileAdminSSOLoginController {

//    @PostMapping("")
//    public ResponseEntity<?> authenticate(
//            @ModelAttribute LoginRequest authRequest) {
//        // Prepare headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.add("api-version", "1");
//
//        // Prepare form data
//        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
//        formData.add("username", authRequest.getUsername());
//        formData.add("password", authRequest.getPassword());
//
//        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
//
//        // Create a new instance of HttpClient with a cookie store
//        BasicCookieStore cookieStore = new BasicCookieStore();
//        CloseableHttpClient httpClient = HttpClients.custom()
//                .setDefaultCookieStore(cookieStore)
//                .build();
//
//        // Use HttpComponentsClientHttpRequestFactory with the custom HttpClient
//        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
//        requestFactory.setHttpClient(httpClient);
//
//        RestTemplate restTemplate = new RestTemplate(requestFactory);
//
//        // Make the POST request
//        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
//                "https://api-stage.doptor.gov.bd/api/user/verify",
//                requestEntity,
//                String.class
//        );
//
//        System.out.println(responseEntity);
//
//        // Extract the JWT token from cookies
//        String jwtToken = null;
//        for (Cookie cookie : cookieStore.getCookies()) {
//            if ("Authorization".equals(cookie.getName())) {
//                jwtToken = cookie.getValue();
//                break;
//            }
//        }
//
//        // Parse the JSON response
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode rootNode;
//        try {
//            rootNode = objectMapper.readTree(responseEntity.getBody());
//
//            // Add the extra fields
//            ((ObjectNode) rootNode).put("l", "gro");
//            ((ObjectNode) rootNode).put("token", jwtToken);
//
//            // Return the modified response
//            return ResponseEntity.ok(rootNode);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error parsing JSON response");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
