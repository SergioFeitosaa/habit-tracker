package br.com.habittracker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/** Tests the real security, service and persistence stack against a disposable PostgreSQL database. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationIT {

  private static final String PASSWORD = "Senha-teste-123!";
  private static final String DAY = "2020-01-01";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper json;

  private String encode(Object value) throws Exception {
    return json.writeValueAsString(value);
  }

  private JsonNode response(MvcResult result) throws Exception {
    return json.readTree(result.getResponse().getContentAsString());
  }

  private String register() throws Exception {
    String email = "teste-" + UUID.randomUUID() + "@example.com";
    mvc
      .perform(
        post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            encode(
              Map.of(
                "name",
                "Pessoa Teste",
                "email",
                email,
                "password",
                PASSWORD
              )
            )
          )
      )
      .andExpect(status().isCreated());
    return email;
  }

  private String login(String email, String password) throws Exception {
    MvcResult result = mvc
      .perform(
        post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(encode(Map.of("email", email, "password", password)))
      )
      .andExpect(status().isOk())
      .andReturn();
    return response(result).get("token").asText();
  }

  private long createHabit(String token) throws Exception {
    MvcResult result = mvc
      .perform(
        post("/api/habits")
          .header("Authorization", "Bearer " + token)
          .contentType(MediaType.APPLICATION_JSON)
          .content(encode(Map.of("name", "Leitura")))
      )
      .andExpect(status().isCreated())
      .andReturn();
    return response(result).get("id").asLong();
  }

  @Test
  void registrationValidatesInputAndRejectsDuplicateEmail() throws Exception {
    mvc
      .perform(
        post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            encode(Map.of("name", "", "email", "invalido", "password", "123"))
          )
      )
      .andExpect(status().isBadRequest());
    String email = register();
    mvc
      .perform(
        post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            encode(
              Map.of(
                "name",
                "Outra Pessoa",
                "email",
                email.toUpperCase(),
                "password",
                PASSWORD
              )
            )
          )
      )
      .andExpect(status().isConflict());
  }

  @Test
  void loginRejectsInvalidCredentials() throws Exception {
    String email = register();
    mvc
      .perform(
        post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            encode(Map.of("email", email, "password", "senha-incorreta"))
          )
      )
      .andExpect(status().isUnauthorized());
    login(email, PASSWORD);
  }

  @Test
  void anonymousAndInvalidTokenAreRejected() throws Exception {
    mvc.perform(get("/api/habits")).andExpect(status().isUnauthorized());
    mvc
      .perform(
        get("/api/habits").header("Authorization", "Bearer token-invalido")
      )
      .andExpect(status().isUnauthorized());
  }

  @Test
  void anotherUserCannotReadOrChangeHabit() throws Exception {
    String owner = login(register(), PASSWORD);
    String other = login(register(), PASSWORD);
    long id = createHabit(owner);
    mvc
      .perform(
        get("/api/habits/{id}", id).header("Authorization", "Bearer " + other)
      )
      .andExpect(status().isNotFound());
    mvc
      .perform(
        delete("/api/habits/{id}", id).header(
          "Authorization",
          "Bearer " + other
        )
      )
      .andExpect(status().isNotFound());
    mvc
      .perform(
        get("/api/habits/{id}", id).header("Authorization", "Bearer " + owner)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value("Leitura"));
  }

  @Test
  void completionIsIdempotentAndFutureDatesAreRejected() throws Exception {
    String token = login(register(), PASSWORD);
    long id = createHabit(token);
    for (int i = 0; i < 2; i++) {
      mvc
        .perform(
          put("/api/habits/{id}/completions/{date}", id, DAY).header(
            "Authorization",
            "Bearer " + token
          )
        )
        .andExpect(status().isNoContent());
    }
    String future = LocalDate.now(ZoneId.of("America/Recife"))
      .plusDays(1)
      .toString();
    mvc
      .perform(
        put("/api/habits/{id}/completions/{date}", id, future).header(
          "Authorization",
          "Bearer " + token
        )
      )
      .andExpect(status().isBadRequest());
    mvc
      .perform(
        get("/api/habits/{id}/completions", id)
          .param("from", "2020-01-01")
          .param("to", "2020-12-31")
          .header("Authorization", "Bearer " + token)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.total").value(1))
      .andExpect(jsonPath("$.dates[0]").value(DAY));
  }

  @Test
  void archivedHabitRetainsHistoryAndRejectsNewCompletions() throws Exception {
    String token = login(register(), PASSWORD);
    long id = createHabit(token);
    mvc
      .perform(
        put("/api/habits/{id}/completions/{date}", id, DAY).header(
          "Authorization",
          "Bearer " + token
        )
      )
      .andExpect(status().isNoContent());
    mvc
      .perform(
        delete("/api/habits/{id}", id).header(
          "Authorization",
          "Bearer " + token
        )
      )
      .andExpect(status().isNoContent());
    mvc
      .perform(
        get("/api/habits/{id}/completions", id)
          .param("from", "2020-01-01")
          .param("to", "2020-12-31")
          .header("Authorization", "Bearer " + token)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.total").value(1));
    mvc
      .perform(
        put("/api/habits/{id}/completions/{date}", id, DAY).header(
          "Authorization",
          "Bearer " + token
        )
      )
      .andExpect(status().isConflict());
  }
}
