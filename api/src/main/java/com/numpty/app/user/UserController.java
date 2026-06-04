package com.numpty.app.user;

import com.numpty.app.user.dto.UserLoginRequest;
import com.numpty.app.user.dto.UserLoginResponse;
import com.numpty.app.user.dto.UserRegisterRequest;
import com.numpty.app.user.dto.UserRegisterResponse;
import com.numpty.framework.web.api.Request;
import com.numpty.framework.web.api.ResponseEntity;

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public ResponseEntity<UserRegisterResponse> register(Request request) {
        return ResponseEntity.created()
            .body(userService.register(request.convertBodyToDTO(UserRegisterRequest.class)));
    }

    public ResponseEntity<UserLoginResponse> login(Request request) {
        return ResponseEntity.ok()
            .body(userService.login(request.convertBodyToDTO(UserLoginRequest.class)));
    }
}
