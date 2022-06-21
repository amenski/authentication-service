package it.aman.authenticationservice.service.security;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ModelLogin {
    private String username;
    // TODO how to mask this
    private String password;

}
