package ua.edu.ukma.cs.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectMapperHolder {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public static ObjectMapper get() {
        return MAPPER;
    }
}
