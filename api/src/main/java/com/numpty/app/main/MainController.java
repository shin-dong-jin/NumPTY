package com.numpty.app.main;

import com.numpty.framework.web.api.Request;
import com.numpty.framework.web.api.ResponseEntity;

public class MainController {

    public Object view(Request request) {
        return "index";
    }

    public Object favicon(Request request) {
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Void> healthCheck(Request request) {
        return ResponseEntity.ok().build();
    }
}
