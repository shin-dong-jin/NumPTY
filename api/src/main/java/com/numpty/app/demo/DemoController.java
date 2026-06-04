package com.numpty.app.demo;

import com.numpty.framework.web.api.ModelAndView;
import com.numpty.framework.web.api.Request;
import com.numpty.framework.web.api.ResponseEntity;

import java.math.BigInteger;
import java.util.Map;

public class DemoController {
    public ModelAndView demoPath(Request request) {
        return new ModelAndView("demo-path").putAll(
                Map.of(
                        "id", request.getPathVariable("id").asString(),
                        "query", request.getPathVariable("query").asString()
                )
        );
    }

    public ModelAndView demo(Request request) {
        return new ModelAndView("demo").putAll(
                Map.of(
                        "data",
                        new DemoDTO(request.getQueryString("id").asString(),
                                request.getQueryString("message").asString(),
                                new BigInteger(request.getQueryString("integer").asString()))
                )
        );
    }

    public ResponseEntity<DemoDTO> demoPost(Request request) {
        return ResponseEntity.ok().body(request.convertBodyToDTO(DemoDTO.class));
    }
}
