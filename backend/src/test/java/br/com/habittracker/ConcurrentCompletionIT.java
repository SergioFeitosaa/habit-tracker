package br.com.habittracker;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.habittracker.config.JwtService;
import br.com.habittracker.repository.CompletionRepository;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

/** Uses committed fixtures and independent transactions to exercise a real PostgreSQL conflict. */
@SpringBootTest
@AutoConfigureMockMvc
class ConcurrentCompletionIT {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private JdbcTemplate jdbc;

  @Autowired
  private JwtService jwt;

  @Autowired
  private CompletionRepository completions;

  @Autowired
  private TransactionTemplate transaction;

  @Test
  void simultaneousMarkReturnsSuccessAndPersistsOnlyOneCompletion()
    throws Exception {
    String email = "concurrent-" + UUID.randomUUID() + "@example.com";
    Long userId = jdbc.queryForObject(
      "INSERT INTO app_users (name, email, password_hash) VALUES (?, ?, ?) RETURNING id",
      Long.class,
      "Pessoa Teste",
      email,
      "unused-test-hash"
    );
    Long habitId = null;
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch inserted = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    Future<?> first = null;
    Future<?> second = null;
    try {
      habitId = jdbc.queryForObject(
        "INSERT INTO habits (user_id, name) VALUES (?, ?) RETURNING id",
        Long.class,
        userId,
        "Leitura"
      );
      final Long id = habitId;
      LocalDate date = LocalDate.of(2020, 1, 1);
      first = executor.submit(() ->
        transaction.executeWithoutResult(status -> {
          completions.insertIfAbsent(id, date);
          inserted.countDown();
          try {
            if (
              !release.await(20, TimeUnit.SECONDS)
            ) throw new IllegalStateException("Timed out waiting to commit");
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
          }
        })
      );
      assertTrue(
        inserted.await(10, TimeUnit.SECONDS),
        "First transaction did not insert"
      );
      String token = jwt.create(email);
      second = executor.submit(() -> {
        try {
          mvc
            .perform(
              put("/api/habits/{id}/completions/{date}", id, date).header(
                "Authorization",
                "Bearer " + token
              )
            )
            .andExpect(status().isNoContent());
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      });

      // Wait until the competing operation blocks on a PostgreSQL lock.
      boolean blocked = false;
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
      while (System.nanoTime() < deadline && !blocked) {
        blocked = Boolean.TRUE.equals(
          jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM pg_stat_activity WHERE datname = current_database() AND pid <> pg_backend_pid() AND state = 'active' AND wait_event_type = 'Lock' AND (query ILIKE '%insert%completions%' OR query ILIKE '%for%update%'))",
            Boolean.class
          )
        );
        if (!blocked) Thread.sleep(25);
      }
      assertTrue(
        blocked,
        "Competing operation did not reach the database lock"
      );
      release.countDown();
      first.get(10, TimeUnit.SECONDS);
      second.get(10, TimeUnit.SECONDS);
      assertEquals(
        1,
        jdbc.queryForObject(
          "SELECT count(*) FROM completions WHERE habit_id = ? AND completion_date = ?",
          Integer.class,
          id,
          date
        )
      );
    } finally {
      release.countDown();
      executor.shutdown();
      if (!executor.awaitTermination(25, TimeUnit.SECONDS)) {
        executor.shutdownNow();
        assertTrue(
          executor.awaitTermination(10, TimeUnit.SECONDS),
          "Workers failed to stop"
        );
      }
      if (habitId != null) {
        jdbc.update("DELETE FROM completions WHERE habit_id = ?", habitId);
        jdbc.update("DELETE FROM habits WHERE id = ?", habitId);
      }
      jdbc.update("DELETE FROM app_users WHERE id = ?", userId);
    }
  }
}
