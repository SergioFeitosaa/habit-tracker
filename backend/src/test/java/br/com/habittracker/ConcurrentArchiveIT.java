package br.com.habittracker;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.habittracker.config.JwtService;
import br.com.habittracker.service.HabitService;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

/** Uses separate committed transactions and waits for a real PostgreSQL row lock. */
@SpringBootTest
@AutoConfigureMockMvc
class ConcurrentArchiveIT {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private JdbcTemplate jdbc;

  @Autowired
  private JwtService jwt;

  @Autowired
  private TransactionTemplate transaction;

  @Autowired
  private HabitService habits;

  @ParameterizedTest
  @ValueSource(strings = { "mark", "unmark" })
  void completionChangeWaitsForArchiveAndRejectsArchivedHabit(String operation)
    throws Exception {
    String email = "archive-" + UUID.randomUUID() + "@example.com";
    Long userId = jdbc.queryForObject(
      "INSERT INTO app_users (name, email, password_hash) VALUES (?, ?, ?) RETURNING id",
      Long.class,
      "Pessoa Teste",
      email,
      "unused-test-hash"
    );
    Long habitId = null;
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch archived = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    try {
      habitId = jdbc.queryForObject(
        "INSERT INTO habits (user_id, name) VALUES (?, ?) RETURNING id",
        Long.class,
        userId,
        "Leitura"
      );
      final Long id = habitId;
      LocalDate date = LocalDate.of(2020, 1, 1);
      if (operation.equals("unmark")) {
        jdbc.update(
          "INSERT INTO completions (habit_id, completion_date) VALUES (?, ?)",
          id,
          date
        );
      }
      Future<?> archive = executor.submit(() ->
        transaction.executeWithoutResult(status -> {
          jdbc.update("UPDATE habits SET archived = true WHERE id = ?", id);
          archived.countDown();
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
        archived.await(10, TimeUnit.SECONDS),
        "Archive did not acquire the lock"
      );
      String token = jwt.create(email);
      Future<?> change = executor.submit(() -> {
        try {
          var request = operation.equals("mark")
            ? put("/api/habits/{id}/completions/{date}", id, date)
            : delete("/api/habits/{id}/completions/{date}", id, date);
          mvc
            .perform(request.header("Authorization", "Bearer " + token))
            .andExpect(status().isConflict());
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      });
      boolean blocked = false;
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
      while (System.nanoTime() < deadline && !blocked) {
        blocked = Boolean.TRUE.equals(
          jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM pg_stat_activity WHERE datname = current_database() AND pid <> pg_backend_pid() AND state = 'active' AND wait_event_type = 'Lock' AND query ILIKE '%habits%' AND query ILIKE '%for%update%')",
            Boolean.class
          )
        );
        if (!blocked) Thread.sleep(25);
      }
      assertTrue(
        blocked,
        "Completion operation did not wait for the habit lock"
      );
      release.countDown();
      archive.get(10, TimeUnit.SECONDS);
      change.get(10, TimeUnit.SECONDS);
      assertEquals(
        operation.equals("unmark") ? 1 : 0,
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

  @Test
  void archiveWaitsForMarkAndPreservesItsCompletion() throws Exception {
    String email = "mark-first-" + UUID.randomUUID() + "@example.com";
    Long userId = jdbc.queryForObject(
      "INSERT INTO app_users (name, email, password_hash) VALUES (?, ?, ?) RETURNING id",
      Long.class,
      "Pessoa Teste",
      email,
      "unused-test-hash"
    );
    Long habitId = null;
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch marked = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    try {
      habitId = jdbc.queryForObject(
        "INSERT INTO habits (user_id, name) VALUES (?, ?) RETURNING id",
        Long.class,
        userId,
        "Leitura"
      );
      final Long id = habitId;
      LocalDate date = LocalDate.of(2020, 1, 1);
      Future<?> mark = executor.submit(() ->
        transaction.executeWithoutResult(status -> {
          habits.mark(email, id, date);
          marked.countDown();
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
        marked.await(10, TimeUnit.SECONDS),
        "Mark did not acquire the lock"
      );
      String token = jwt.create(email);
      Future<?> archive = executor.submit(() -> {
        try {
          mvc
            .perform(
              delete("/api/habits/{id}", id).header(
                "Authorization",
                "Bearer " + token
              )
            )
            .andExpect(status().isNoContent());
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      });
      boolean blocked = false;
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
      while (System.nanoTime() < deadline && !blocked) {
        blocked = Boolean.TRUE.equals(
          jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM pg_stat_activity WHERE datname = current_database() AND pid <> pg_backend_pid() AND state = 'active' AND wait_event_type = 'Lock' AND query ILIKE '%habits%' AND query ILIKE '%for%update%')",
            Boolean.class
          )
        );
        if (!blocked) Thread.sleep(25);
      }
      assertTrue(blocked, "Archive did not wait for the habit lock");
      release.countDown();
      mark.get(10, TimeUnit.SECONDS);
      archive.get(10, TimeUnit.SECONDS);
      assertTrue(
        Boolean.TRUE.equals(
          jdbc.queryForObject(
            "SELECT archived FROM habits WHERE id = ?",
            Boolean.class,
            id
          )
        )
      );
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
