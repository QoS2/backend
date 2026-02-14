package com.app.questofseoul.security;

import com.app.questofseoul.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Google(OIDC) 로그인 시 userId를 사용자 속성에 추가합니다.
 * issuer-uri로 설정된 OIDC 프로바이더는 기본 OidcUserService가 사용되므로,
 * OidcUserConverter를 통해 userId를 추가합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final AuthService authService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!"google".equals(registrationId)) {
            return oidcUser;
        }

        String googleSub = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        UUID userId = authService.getOrCreateUserFromGoogle(googleSub, email, name);

        Map<String, Object> attributes = new HashMap<>(oidcUser.getAttributes());
        attributes.put(CustomOAuth2UserService.USER_ID_ATTR, userId.toString());

        return new OidcUserWithUserId(oidcUser, attributes);
    }

    /**
     * OidcUser를 감싸며 userId 속성을 추가합니다.
     */
    private static class OidcUserWithUserId implements OidcUser {
        private final OidcUser delegate;
        private final Map<String, Object> attributes;

        OidcUserWithUserId(OidcUser delegate, Map<String, Object> attributes) {
            this.delegate = delegate;
            this.attributes = Map.copyOf(attributes);
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <A> A getAttribute(String name) {
            if (attributes.containsKey(name)) {
                return (A) attributes.get(name);
            }
            return delegate.getAttribute(name);
        }

        @Override
        public org.springframework.security.oauth2.core.oidc.OidcIdToken getIdToken() {
            return delegate.getIdToken();
        }

        @Override
        public org.springframework.security.oauth2.core.oidc.OidcUserInfo getUserInfo() {
            return delegate.getUserInfo();
        }

        @Override
        public java.util.Map<String, Object> getClaims() {
            return delegate.getClaims();
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return delegate.getAuthorities();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }
    }
}
