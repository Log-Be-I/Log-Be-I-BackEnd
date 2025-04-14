package com.springboot.oauth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class OAuthAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;

    public OAuthAuthenticationToken(Object principal) {
        super(null);
        this.principal = principal;
        setAuthenticated(false);
    }

    public OAuthAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null; // OAuth는 비밀번호 없음
    }

    @Override
    public Object getPrincipal() {
        return principal; // 보통은 이메일
    }
}
