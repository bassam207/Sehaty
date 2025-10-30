package com.Sehaty.Sehaty.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * Represents a user in the Sehaty application.
 * A user can be a patient or a doctor (role management can be added later).
 *
 * Each user can:
 * - Upload multiple medical files.
 * - Share those files with others (via SharedRecords).
 */
@Entity
@Builder
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String profileImageUrl;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<MedicalFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SharedRecords> shares = new ArrayList<>();

    // ==============================
    // 👇 UserDetails Implementation
    // ==============================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // حالياً بدون roles
    }

    @Override
    public String getUsername() {
        return email; // نستخدم الإيميل كـ username
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