package com.gruppo3.ai.lab3.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Collection<? extends GrantedAuthority> authorities;
    private String username;
    private String password;
    private Boolean accountNonExpired, accountNonLocked, credentialsNonExpired, enabled;

    public CustomUserDetails(User byUsername) {
        this.username = byUsername.getUsername();
        this.password = byUsername.getPassword();
        List<GrantedAuthority> auths = new ArrayList<>();
        for (Role role : byUsername.getRoles())
            auths.add(new SimpleGrantedAuthority(role.getName()));
        this.authorities = auths;
        this.accountNonExpired = byUsername.getAccountNonExpired();
        this.accountNonLocked = byUsername.getAccountNonLocked();
        this.credentialsNonExpired = byUsername.getCredentialsNonExpired();
        this.enabled = byUsername.getEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
