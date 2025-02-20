package hr.ekufrin.training_api.controller;

import hr.ekufrin.training_api.model.User;
import hr.ekufrin.training_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    @PostMapping("/details")
    public ResponseEntity<User> getUserDetails(@RequestBody Map<String,String> request) {
        String email = request.get("email");
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
