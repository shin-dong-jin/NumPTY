package com.numpty.framework.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathMatcher {

    private final Pattern regex;
    private final List<String> pathKeys;

    public PathMatcher(String routePattern) {
        this.pathKeys = new ArrayList<>();

        Matcher matcher = Pattern.compile("\\{([^/]+)\\}").matcher(routePattern);
        StringBuilder regexBuilder = new StringBuilder("^");

        while (matcher.find()) {
            pathKeys.add(matcher.group(1));
            matcher.appendReplacement(regexBuilder, "([^/]+)");
        }
        matcher.appendTail(regexBuilder);
        regexBuilder.append("$");

        this.regex = Pattern.compile(regexBuilder.toString());
    }

    public Map<String, String> matchPath(String requestPath) {
        Matcher matcher = regex.matcher(requestPath);

        if (!matcher.matches()) {
            return null;
        }

        Map<String, String> pathVariables = new HashMap<>();

        for (int i = 0; i < pathKeys.size(); i++) {
            pathVariables.put(pathKeys.get(i), matcher.group(i + 1));
        }

        return pathVariables;
    }
}
