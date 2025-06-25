package ua.edu.ukma.cs.services;

import ua.edu.ukma.cs.api.request.RegisterUserRequestDto;

public class RegisterService {
    private final HttpService httpService;

    public RegisterService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void register(RegisterUserRequestDto dto) throws Exception {
        var response = httpService.postJsonFullResponse("/register", dto);
        if (response.statusCode() != 200) {
            throw new RuntimeException("Registration failed: HTTP " + response.statusCode());
        }
    }
}
