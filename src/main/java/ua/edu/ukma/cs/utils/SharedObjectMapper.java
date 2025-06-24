package ua.edu.ukma.cs.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SharedObjectMapper {
    public static final ObjectMapper S = new ObjectMapper().findAndRegisterModules();
}
