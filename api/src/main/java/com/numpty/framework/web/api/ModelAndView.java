package com.numpty.framework.web.api;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {

    private String viewName;
    private Map<String, Object> model = new HashMap<>();

    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public void addModel(String key, Object value) {
        model.put(key, value);
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public ModelAndView put(String key, Object value) {
        model.put(key, value);

        return this;
    }

    public ModelAndView putAll(Map<?, ?> map) {
        if (map != null) {
            map.forEach((key, value) -> put(String.valueOf(key), value));
        }

        return this;
    }
}
