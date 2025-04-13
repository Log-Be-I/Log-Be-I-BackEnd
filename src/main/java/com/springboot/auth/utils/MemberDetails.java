package com.springboot.auth.utils;

import com.springboot.member.entity.Member;
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
        this.authorityUtils = authorityUtils;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorityUtils.createAuthorities(this.getRoles());
    }

    @Override
    public String getPassword() {
        return "";
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