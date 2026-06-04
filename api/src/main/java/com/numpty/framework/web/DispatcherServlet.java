package com.numpty.framework.web;

import com.numpty.framework.context.ApplicationContext;
import com.numpty.framework.web.api.ModelAndView;
import com.numpty.framework.web.http.HttpMethod;
import com.numpty.framework.web.http.HttpStatus;
import com.numpty.framework.exception.CoreException;
import com.numpty.framework.exception.ExceptionResolvers;
import com.numpty.framework.web.handler.HandlerAdapter;
import com.numpty.framework.web.handler.HandlerAdapters;
import com.numpty.framework.web.handler.HandlerFunction;
import com.numpty.framework.web.view.ViewEngine;
import com.numpty.framework.web.view.ViewResolver;
import com.numpty.framework.web.router.RouterFunction;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    private final ApplicationContext applicationContext;
    private HandlerAdapters handlerAdapters;
    private RouterFunction router;
    private ViewResolver viewResolver;
    private ViewEngine viewEngine;
    private ExceptionResolvers exceptionResolvers;

    public DispatcherServlet(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        handlerAdapters = applicationContext.getBean(HandlerAdapters.class);
        router = applicationContext.getBean(RouterFunction.class);
        viewResolver = applicationContext.getBean(ViewResolver.class);
        viewEngine = applicationContext.getBean(ViewEngine.class);
        exceptionResolvers = applicationContext.getBean(ExceptionResolvers.class);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            HttpMethod method = HttpMethod.fromString(req.getMethod());

            HandlerFunction handler = router.route(req);

            if (handler == null) {
                throw new CoreException(
                        "Requested endpoint does not exist: " + "[" + method + "] " + req.getRequestURI(),
                        HttpStatus.NOT_FOUND
                );
            }

            HandlerAdapter handlerAdapter = handlerAdapters.findHandlerAdapter(handler);

            ModelAndView result = handlerAdapter.handle(req, resp, handler);

            if (result == null) {
                return;
            }

            String viewPath = viewResolver.resolve(result.getViewName());
            Map<String, Object> model = result.getModel();

            viewEngine.render(viewPath, model, resp);
        } catch (Exception e) {
            exceptionResolvers.findAndResolve(e, req, resp);
        }
    }
}
