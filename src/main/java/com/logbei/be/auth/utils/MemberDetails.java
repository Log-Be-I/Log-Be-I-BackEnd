package com.logbei.be.auth.utils;

import com.logbei.be.member.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class MemberDetails extends Member implements UserDetails {
    private final CustomAuthorityUtils authorityUtils;

    public MemberDetails(Member member, CustomAuthorityUtils authorityUtils) {
        super();
        this.setMemberId(member.getMemberId());
        this.setEmail(member.getEmail());
        this.setRoles(member.getRoles());
        this.setRefreshToken(member.getRefreshToken());
        this.authorityUtils = authorityUtils;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorityUtils.createAuthorities(this.getRoles());
    }

    @Override
    public String getPassword() {
        return getRefreshToken();
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}