package br.com.habittracker.service;
import br.com.habittracker.config.JwtService;
import br.com.habittracker.dto.ApiDtos.*;
import br.com.habittracker.entity.AppUser;
import br.com.habittracker.exception.ApiException;
import br.com.habittracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
@Service
public class AuthService {
 private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
 public AuthService(UserRepository users,PasswordEncoder encoder,JwtService jwt){this.users=users;this.encoder=encoder;this.jwt=jwt;}
 private String normalize(String email){return email.trim().toLowerCase(Locale.ROOT);}
 @Transactional public void register(Register input){
  String email=normalize(input.email());
  if(input.password().getBytes(StandardCharsets.UTF_8).length>72) throw new ApiException(HttpStatus.BAD_REQUEST,"Senha deve ter no máximo 72 bytes em UTF-8.");
  if(users.existsByEmail(email)) throw new ApiException(HttpStatus.CONFLICT,"E-mail já cadastrado.");
  users.save(new AppUser(input.name().trim(),email,encoder.encode(input.password())));
 }
 public Token login(Login input){
  AppUser user=users.findByEmail(normalize(input.email())).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"Credenciais inválidas."));
  if(!encoder.matches(input.password(),user.getPasswordHash())) throw new ApiException(HttpStatus.UNAUTHORIZED,"Credenciais inválidas.");
  return new Token(jwt.create(user.getEmail()),user.getName());
 }
}
