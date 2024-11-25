package com.grs.mobileApp.config;

import com.grs.api.config.security.*;
import com.grs.api.model.UserInformation;
import com.grs.core.dao.GrsRoleDAO;
import com.grs.core.dao.UserDAO;
import com.grs.core.domain.grs.GrsRole;
import com.grs.core.domain.projapoti.User;
import com.grs.core.service.FcmService;
import com.grs.utils.BanglaConverter;
import com.grs.utils.BeanUtil;
import com.grs.utils.Constant;
import com.grs.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.grs.api.config.security.TokenAuthenticationServiceUtil.constuctJwtToken;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AuthUtilForMobileAPI {

    private final UserDAO userDAO;
    private final OISFUserDetailsServiceImpl oisfUserDetailsService;
    private final GrsRoleDAO grsRoleDAO;

    public Authentication doAuthentication(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();

        log.info(name);
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

    public String getToken(Authentication authentication,
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

        String deviceToken = request.getParameter("device_token");
        if (StringUtil.isValidString(deviceToken)) {
            FcmService fcmService = BeanUtil.bean(FcmService.class);
            fcmService.registerDeviceToken(deviceToken, name);
            userInformation.setIsMobileLogin(true);
        } else {
            userInformation.setIsMobileLogin(false);
        }
        String JWT = constuctJwtToken(name, permissionNamesSet, userInformation);
        Cookie cookie = new Cookie(Constant.HEADER_STRING, JWT);
        cookie.setMaxAge(Constant.COOKIE_EXPIRATION_TIME);
        response.addCookie(cookie);

        return JWT;

    }
}
