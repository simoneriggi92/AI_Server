package com.gruppo3.ai.lab3.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

public class UpdateCredentialEntry implements Serializable {

    @NotNull(message = "Username must not be null")
    @NotEmpty(message = "Username must not be empty")
    private String username;

    @NotNull(message = "Password must not be null")
    @NotEmpty(message = "Password must not be empty")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[#$^+=!*()@%&]).{8,15}.$",
            message = "Password must have at least one lowercase letter, one capitol letter, one number and a special\n" + "character")
    private String password;

    @NotNull(message = "OldPassword must not be null")
    @NotEmpty(message = "OldPassword must not be empty")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[#$^+=!*()@%&]).{8,15}.$",
            message = "OldPassword must have at least one lowercase letter, one capitol letter, one number and a special\n" + "character")
    private String oldPassword;

    @NotNull(message = "ConfirmPassword must not be null")
    @NotEmpty(message = "ConfirmPassword must not be empty")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[#$^+=!*()@%&]).{8,15}.$",
            message = "ConfirmPassword must have at least one lowercase letter, one capitol letter, one number and a special\n" + "character")
    private String confirmNewPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
}
