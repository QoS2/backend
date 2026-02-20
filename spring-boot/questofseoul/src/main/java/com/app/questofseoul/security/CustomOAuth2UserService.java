package com.app.questofseoul.security;

import com.app.questofseoul.domain.enums.UserRole;
import com.app.questofseoul.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String GOOGLE_SUB_ATTR = "sub";
    private static final String GOOGLE_EMAIL_ATTR = "email";
    private static final String GOOGLE_NAME_ATTR = "name";
    public static final String USER_ID_ATTR = "userId";
    public static final String USER_ROLE_ATTR = "userRole";

    private final AuthService authService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!"google".equals(registrationId)) {
            return oauth2User;
        }

        String googleSub = oauth2User.getAttribute(GOOGLE_SUB_ATTR);
        String email = oauth2User.getAttribute(GOOGLE_EMAIL_ATTR);
        String name = oauth2User.getAttribute(GOOGLE_NAME_ATTR);

        UUID userId = authService.getOrCreateUserFromGoogle(googleSub, email, name);
        UserRole role = authService.resolveRole(userId);

        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        attributes.put(USER_ID_ATTR, userId.toString());
        attributes.put(USER_ROLE_ATTR, role.name());

        String nameAttributeKey = userRequest.getClientRegistration()
            .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Set<GrantedAuthority> mergedAuthorities = new HashSet<>(oauth2User.getAuthorities());
        mergedAuthorities.addAll(SecurityRoleUtils.toAuthorities(role));

        return new DefaultOAuth2User(
            mergedAuthorities,
            attributes,
            nameAttributeKey
        );
    }
}
