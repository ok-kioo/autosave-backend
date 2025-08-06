package com.signature.autosave.modules.user.domain.entity;

import com.signature.autosave.modules.user.domain.enums.SubscriptionPlan;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(unique = true)
    private String nickName;

    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlan subscriptionPlan;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return switch (this.subscriptionPlan) {
            case FREE -> List.of(new SimpleGrantedAuthority(SubscriptionPlan.FREE.getPlanName()));

            case BASIC -> List.of(new SimpleGrantedAuthority(SubscriptionPlan.BASIC.getPlanName()),
                    new SimpleGrantedAuthority(SubscriptionPlan.FREE.getPlanName()));

            case PREMIUM -> List.of(new SimpleGrantedAuthority(SubscriptionPlan.PREMIUM.getPlanName()),
                    new SimpleGrantedAuthority(SubscriptionPlan.BASIC.getPlanName()),
                    new SimpleGrantedAuthority(SubscriptionPlan.FREE.getPlanName()));

        };
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.password;
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
