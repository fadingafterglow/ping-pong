package ua.edu.ukma.cs.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SecurityContext {
    private int userId;
    private String username;
}
