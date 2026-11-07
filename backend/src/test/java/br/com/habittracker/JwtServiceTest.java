package br.com.habittracker;

import static org.junit.jupiter.api.Assertions.*;

import br.com.habittracker.config.JwtService;
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
