package ua.edu.ukma.cs.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {
    private String login;
    private String password;
}
