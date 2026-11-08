package br.com.habittracker;

import static org.junit.jupiter.api.Assertions.*;

import br.com.habittracker.config.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  @Test
  void signsAndReadsSubject() {
    JwtService jwt = new JwtService("12345678901234567890123456789012");
    assertEquals(
      "user@example.com",
      jwt.subject(jwt.create("user@example.com"))
    );
  }

  @Test
  void rejectsExpiredSignedToken() {
    String secret = "12345678901234567890123456789012";
    JwtService jwt = new JwtService(secret);
    Instant now = Instant.now();
    String expired = Jwts.builder()
      .subject("user@example.com")
      .issuedAt(Date.from(now.minusSeconds(120)))
      .expiration(Date.from(now.minusSeconds(60)))
      .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
      .compact();
    assertThrows(ExpiredJwtException.class, () -> jwt.subject(expired));
  }

  @Test
  void issuedTokenHasEightHourLifetime() {
    String secret = "12345678901234567890123456789012";
    JwtService jwt = new JwtService(secret);
    var claims = Jwts.parser()
      .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
      .build()
      .parseSignedClaims(jwt.create("user@example.com"))
      .getPayload();
    assertEquals(
      8 * 60 * 60 * 1000L,
      claims.getExpiration().getTime() - claims.getIssuedAt().getTime()
    );
  }

  @Test
  void rejectsShortSecret() {
    assertThrows(IllegalArgumentException.class, () -> new JwtService("small"));
  }

  @Test
  void rejectsTampering() {
    JwtService jwt = new JwtService("12345678901234567890123456789012");
    String[] parts = jwt.create("user@example.com").split("\\.");
    String forged =
      parts[0] +
      "." +
      parts[1] +
      "." +
      (parts[2].startsWith("A") ? "B" : "A") +
      parts[2].substring(1);
    assertThrows(Exception.class, () -> jwt.subject(forged));
  }
}
