package com.numpty.framework.web.response;

import com.numpty.framework.web.api.ModelAndView;
import com.numpty.framework.web.http.HttpStatus;
import com.numpty.framework.exception.CoreException;
import com.numpty.framework.web.http.MutableRequest;
import com.numpty.framework.web.http.ServerRequest;
import com.numpty.framework.web.http.ServerResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

public class ViewReturnValueHandler implements ReturnValueHandler {

    private final Map<Class<?>, TriFunction<HttpServletRequest, HttpServletResponse, Object, ModelAndView>> strategies = new HashMap<>();

    public ViewReturnValueHandler() {
        strategies.put(void.class, (req, resp, result) -> resp.isCommitted() ? null : new ModelAndView(extractViewName(req)));

        strategies.put(ModelAndView.class, (req, resp, result) -> (ModelAndView) result);

        strategies.put(Map.class, (req, resp, result) -> new ModelAndView(extractViewName(req)).putAll((Map<?, ?>) result));

        strategies.put(String.class, (req, resp, result) -> {
            String path = (String) result;
            return path.startsWith("redirect:") ? new ModelAndView(path) : new ModelAndView(extractViewName(path));
        });
    }

    @Override
    public boolean supports(Class<?> returnType) {
        return returnType == void.class
                || returnType == ModelAndView.class
                || returnType == String.class
                || Map.class.isAssignableFrom(returnType);
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception {
        Class<?> type = result == null ? void.class : result.getClass();
        Class<?> strategyKey = Map.class.isAssignableFrom(type) ? Map.class : type;

        TriFunction<HttpServletRequest, HttpServletResponse, Object, ModelAndView> strategy = strategies.get(strategyKey);

        if (strategy == null) {
            throw new CoreException("Unsupported view type: " + type.getSimpleName(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return strategy.apply(request, response, result);
    }

    private String extractViewName(HttpServletRequest req) {
        String viewName = req.getServletPath();

        if (viewName.startsWith("/")) {
            viewName = viewName.substring(1);
        }

        if (viewName.endsWith(".do")) {
            viewName = viewName.substring(0, viewName.lastIndexOf("."));
        }

        return viewName.isBlank() ? "index" : viewName;
    }

    private String extractViewName(String path) {
        String viewName = path;

        if (viewName.startsWith("/")) {
            viewName = viewName.substring(1);
        }

        if (viewName.endsWith(".do")) {
            viewName = viewName.substring(0, viewName.lastIndexOf("."));
        }

        return viewName.isBlank() ? "index" : viewName;
    }

    @FunctionalInterface
    private interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }
}
