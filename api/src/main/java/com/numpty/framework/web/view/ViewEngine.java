package com.numpty.framework.web.view;

import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.util.Map;

public class ViewEngine {

    private final TemplateEngine templateEngine;

    public ViewEngine() {
        this(TemplateMode.HTML, "UTF-8");
    }

    public ViewEngine(TemplateMode mode, String encoding) {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(mode);
        templateResolver.setCharacterEncoding(encoding);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    public void render(String viewPath, Map<String, Object> model, HttpServletResponse resp) throws IOException {
        Context context = new Context();

        if (model != null) {
            context.setVariables(model);
        }

        resp.setContentType("text/html;charset=UTF-8");
        templateEngine.process(viewPath, context, resp.getWriter());
    }
}
