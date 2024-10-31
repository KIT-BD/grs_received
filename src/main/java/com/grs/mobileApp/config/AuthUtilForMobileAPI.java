package com.grs.mobileApp.config;

import com.grs.api.config.security.CustomAuthenticationToken;
import com.grs.api.config.security.UserDetailsImpl;
import com.grs.api.model.UserInformation;
import com.grs.core.service.FcmService;
import com.grs.utils.BeanUtil;
import com.grs.utils.Constant;
import com.grs.utils.StringUtil;
import org.springframework.security.core.Authentication;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static com.grs.api.config.security.TokenAuthenticationServiceUtil.constuctJwtToken;

public class AuthUtilForMobileAPI {

    public static String constructJWTForMobileAdmin(Authentication authentication,
                                         HttpServletRequest request,
                                         HttpServletResponse response) throws IOException, ServletException {
        UserInformation userInformation;
        String name;
        Set<String> permissionNamesSet;
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            name = authentication.getName();
            permissionNamesSet = authentication.getAuthorities()
                    .stream()
                    .map(permission -> permission.getAuthority())
                    .collect(Collectors.toSet());
            userInformation = userDetails.getUserInformation();

        } catch (Exception e) {
            CustomAuthenticationToken token = (CustomAuthenticationToken) authentication;
            name = token.getName();
            permissionNamesSet = token.getAuthorities()
                    .stream()
                    .map(permission -> permission.getAuthority())
                    .collect(Collectors.toSet());
            userInformation = token.getUserInformation();
        }

        String deviceToken = request.getParameter("device_token");
        if (StringUtil.isValidString(deviceToken)) {
            FcmService fcmService = BeanUtil.bean(FcmService.class);
            fcmService.registerDeviceToken(deviceToken, name);
            userInformation.setIsMobileLogin(true);
        } else {
            userInformation.setIsMobileLogin(false);
        }
        return constuctJwtToken(name, permissionNamesSet, userInformation);

    }
}
