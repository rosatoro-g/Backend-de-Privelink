
package privelink.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import privelink.backend.dto.LoginRequest;
import privelink.backend.dto.LoginResponse;
import privelink.backend.service.AuthService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/estado")
    public String estado() {
        return "API de autenticación de PrivéLink disponible";
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.iniciarSesion(request);

        return ResponseEntity.ok(response);
    }
}
