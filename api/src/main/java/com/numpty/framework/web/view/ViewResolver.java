package com.numpty.framework.web.view;

public class ViewResolver {

    private String prefix = "templates/";
    private String suffix = ".html";

    public ViewResolver() {

    }

    public ViewResolver(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String resolve(String viewName) {
        return prefix + viewName + suffix;
    }
}
