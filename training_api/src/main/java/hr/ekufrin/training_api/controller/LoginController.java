package hr.ekufrin.training_api.controller;

import hr.ekufrin.training_api.jwt.JWTUtil;
import hr.ekufrin.training_api.model.LoginForm;
import hr.ekufrin.training_api.model.User;
import hr.ekufrin.training_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {
    private final UserService userService;
    private final JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@RequestBody LoginForm loginForm) {
        Optional<User> employeeOpt = userService.findByEmail(loginForm.email());
        if (employeeOpt.isPresent()) {
            User employee = employeeOpt.get();
            if (BCrypt.checkpw(loginForm.password(), employee.getPassword())) {
                String token = jwtUtil.generateToken(employee.getEmail());
                Map<String, String> okResponse = new HashMap<>();
                okResponse.put("message", "Login successful");

                return ResponseEntity.ok()
                        .header("Authorization", "Bearer " + token)
                        .body(okResponse);
            }
        }
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Invalid email or password");
        return ResponseEntity.status(401).body(errorResponse);
    }
}
