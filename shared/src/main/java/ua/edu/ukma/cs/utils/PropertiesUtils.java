package ua.edu.ukma.cs.utils;

import lombok.SneakyThrows;

import java.util.Properties;

public class PropertiesUtils {

    private static final String PROPERTIES_FILE = "/application.properties";

    @SneakyThrows
    public static Properties loadProperties() {
        Properties properties = new Properties();
        properties.load(PropertiesUtils.class.getResourceAsStream(PROPERTIES_FILE));
        return properties;
    }
}
