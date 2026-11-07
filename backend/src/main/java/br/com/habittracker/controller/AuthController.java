package br.com.habittracker.controller;
import br.com.habittracker.dto.ApiDtos.*;
import br.com.habittracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final AuthService auth; public AuthController(AuthService auth){this.auth=auth;}
 @PostMapping("/register") public ResponseEntity<Void> register(@Valid @RequestBody Register input){auth.register(input);return ResponseEntity.status(HttpStatus.CREATED).build();}
 @PostMapping("/login") public Token login(@Valid @RequestBody Login input){return auth.login(input);}
}
