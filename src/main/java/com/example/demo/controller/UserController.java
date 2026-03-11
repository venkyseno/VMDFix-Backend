package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{mobile}")
    public ResponseEntity<?> getUser(@PathVariable String mobile) {
        return userRepository.findByMobile(mobile)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (userRepository.findByMobile(user.getMobile()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists");
        }
        user.setRole(UserRole.USER);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        if (user.getMobile() != null && userRepository.findByMobile(user.getMobile()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mobile already registered");
        }
        user.setRole(UserRole.USER);
        user.setSignupProvider(user.getSignupProvider() == null ? "EMAIL" : user.getSignupProvider());
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("/google-signup")
    public ResponseEntity<?> googleSignup(@RequestBody Map<String, String> payload) {
        String mobile = payload.get("mobile");
        return userRepository.findByMobile(mobile)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(userRepository.save(User.builder()
                        .name(payload.getOrDefault("name", "Google User"))
                        .mobile(mobile)
                        .email(payload.get("email"))
                        .password("google-auth")
                        .signupProvider("GOOGLE")
                        .role(UserRole.USER)
                        .build())));
    }

    @PostMapping("/mobile-otp-signup")
    public ResponseEntity<?> mobileOtpSignup(@RequestBody Map<String, String> payload) {
        String mobile = payload.get("mobile");
        String otp = payload.get("otp");
        if (!"123456".equals(otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
        }
        return userRepository.findByMobile(mobile)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(userRepository.save(User.builder()
                        .name(payload.getOrDefault("name", "Mobile User"))
                        .mobile(mobile)
                        .password("otp-login")
                        .signupProvider("MOBILE_OTP")
                        .role(UserRole.USER)
                        .build())));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        Optional<User> optionalUser = userRepository.findByMobile(loginRequest.getMobile());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        User user = optionalUser.get();

        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }

        return ResponseEntity.ok(user);
    }
}
