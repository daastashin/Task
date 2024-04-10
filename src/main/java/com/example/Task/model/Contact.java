package com.example.Task.model;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Schema(description = "Contact info")
public class Contact {

    @Schema(description = "Contact name")
    @NotBlank
    @Size(min = 1, max = 20)
    private String name;

    @Schema(description = "Secret word")
    @NotBlank
    @Size(min = 50, max = 100)
    private String secretWord;

    @Schema(description = "Password")
    @NotBlank
    @Size(min = 1, max = 100)
    private String password;

    @Schema(description = "Is authorized")
    private Boolean authorized = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecretWord() { return secretWord; }

    public void setSecretWord(String secretWord) { this.secretWord = secretWord; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public Boolean getAuthorized() { return authorized; }

    public void setAuthorized(Boolean authorized) { this.authorized = authorized; }
}
