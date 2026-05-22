package sv.edu.udb.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.dto.request.LoginRequest;
import sv.edu.udb.dto.response.JwtResponse;
import sv.edu.udb.dto.response.UserProfileResponse;
import sv.edu.udb.service.IAuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getAuthenticatedProfile() {
        return ResponseEntity.ok(authService.getAuthenticatedProfile());
    }
}
