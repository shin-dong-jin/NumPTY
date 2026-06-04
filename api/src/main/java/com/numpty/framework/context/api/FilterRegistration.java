package com.numpty.framework.context.api;

import jakarta.servlet.Filter;

public record FilterRegistration(String name, Filter filter, String urlPattern) {
}
