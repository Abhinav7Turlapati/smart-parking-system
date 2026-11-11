package com.smartparking.parking.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String role) {
        this.id = id;
        this.email = email;

        String resolvedRole = "USER";
        if (role != null) {
            String trimmed = role.trim();
            if (trimmed.startsWith("ROLE_")) {
                trimmed = trimmed.substring(5);
            }
            if ("ADMIN".equalsIgnoreCase(trimmed)) {
                resolvedRole = "ADMIN";
            }
        }
        this.role = resolvedRole;
        this.authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + this.role),
                new SimpleGrantedAuthority(this.role)
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
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
