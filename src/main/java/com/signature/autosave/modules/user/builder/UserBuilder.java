package com.signature.autosave.modules.user.builder;

import com.signature.autosave.modules.user.domain.entity.User;
import jakarta.validation.constraints.NotBlank;

import static com.signature.autosave.modules.user.domain.enums.SubscriptionPlan.FREE;

public class UserBuilder {
    private String nickName;
    private String email;
    private String password;

    private UserBuilder() {
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public UserBuilder withNickName(@NotBlank String nickName) {
        this.nickName = nickName;
        return this;
    }

    public UserBuilder withEmail(@NotBlank String email) {
        this.email = email;
        return this;
    }

    public UserBuilder withPassword(@NotBlank String password) {
        this.password = password;
        return this;
    }

    public User build() {
        User user = new User();
        user.setNickName(this.nickName);
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setSubscriptionPlan(FREE);
        return user;
    }
}