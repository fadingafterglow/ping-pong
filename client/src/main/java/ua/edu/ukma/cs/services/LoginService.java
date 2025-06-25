package ua.edu.ukma.cs.services;

import ua.edu.ukma.cs.api.request.LoginUserRequestDto;

import java.nio.charset.StandardCharsets;

public class LoginService {
    private final HttpService httpService;

    public LoginService(HttpService httpService) {
        this.httpService = httpService;
    }

    public String login(LoginUserRequestDto dto) throws Exception {
        var response = httpService.post("/login", dto);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Login failed: HTTP " + response.statusCode());
        }
        return new String(response.body(), StandardCharsets.UTF_8);
    }
}
