package com.numpty.app.user;

import com.numpty.app.infra.exception.ArgumentValidationException;
import com.numpty.app.infra.exception.ServiceException;
import com.numpty.app.user.dto.UserLoginRequest;
import com.numpty.app.user.dto.UserLoginResponse;
import com.numpty.app.user.dto.UserRegisterRequest;
import com.numpty.app.user.dto.UserRegisterResponse;
import com.numpty.framework.security.core.jwt.JwtProvider;
import com.numpty.framework.security.core.crypto.PasswordEncoder;
import com.numpty.framework.web.http.HttpStatus;
import java.util.Map;

public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
        JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public UserRegisterResponse register(UserRegisterRequest registerRequest) {
        if (registerRequest.password().length() < 8) {
            throw new ArgumentValidationException("password length must be at least 8");
        }

        if (userRepository.existsUserByEmail(registerRequest.email())) {
            throw new ServiceException("Email already registered", HttpStatus.CONFLICT);
        }

        String encryptedPassword = passwordEncoder.encode(registerRequest.password());

        User user = User.createUser(registerRequest.email(), registerRequest.name(),  encryptedPassword);

        try {
            userRepository.saveUser(user);
        } catch (Exception e) {
            throw new ServiceException("Email already registered", HttpStatus.CONFLICT);
        }

        return new UserRegisterResponse(user.getEmail(), user.getName(), user.getCreatedAt());
    }

    public UserLoginResponse login(UserLoginRequest loginRequest) {
        User user = userRepository.findUserByEmail(loginRequest.email());

        if (user == null) {
            throw new ServiceException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new ServiceException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtProvider.createToken(user.getId().toString(), Map.of(
            "role", user.getRole().toString()
        ));

        return new UserLoginResponse(loginRequest.email(), token);
    }
}
