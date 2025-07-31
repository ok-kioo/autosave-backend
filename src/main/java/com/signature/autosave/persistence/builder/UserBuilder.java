package com.signature.autosave.persistence.builder;

import com.signature.autosave.persistence.entity.User;
import jakarta.validation.constraints.NotBlank;

public class UserBuilder {
    private String name;
    private String email;
    private String password;

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public UserBuilder withName(@NotBlank String name) {
        this.name = name;
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
        user.setName(this.name);
        user.setEmail(this.email);
        user.setPassword(this.password);
        return user;
    }
}
