package com.numpty.framework.boot;

import java.io.InputStream;
import java.util.Properties;

import com.numpty.framework.exception.CoreException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Environment {

    private static final Logger log = LoggerFactory.getLogger(Environment.class);
    private static final Properties properties = new Properties();
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?\\}");

    static {
        try (InputStream inputStream = Environment.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                log.warn(
                        "application.properties is missing: server will start with default configuration.");
            } else {
                properties.load(inputStream);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while load application.properties.", e);
        }
    }

    private Environment() {
        throw new CoreException("Instantiation not allowed for this class.");
    }

    public static String getProperty(String key) {
        String raw = properties.getProperty(key);

        if (raw == null) {
            return null;
        }

        return resolvePlaceHolders(raw);
    }

    public static int getPropertyAsInt(String key) {
        String value = getProperty(key);

        if (value == null || value.isEmpty()) {
            throw new CoreException("Missing value for key: " + key);
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new CoreException("Invalid value for key: " + key, e);
        }
    }

    public static int getPropertyAsInt(String key, int defaultValue) {
        String value = getProperty(key);

        if (value == null || value.isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("{} is not a number, server starts with default value: {}", key, defaultValue);
            return defaultValue;
        }
    }

    public static int getServerPort() {
        return getPropertyAsInt("server.port", 8080);
    }

    private static String resolvePlaceHolders(String value) {
        Matcher matcher = PLACEHOLDER.matcher(value);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String envVar = matcher.group(1);
            String defaultValue = matcher.group(2);

            String resolved = System.getenv(envVar);
            if (resolved == null) {
                resolved = defaultValue != null ? defaultValue : "";
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(resolved));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
