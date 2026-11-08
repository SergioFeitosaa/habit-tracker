package br.com.habittracker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.habittracker.entity.*;
import br.com.habittracker.exception.ApiException;
import br.com.habittracker.repository.*;
import br.com.habittracker.service.HabitService;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.*;

class HabitServiceTest {

  UserRepository users = mock(UserRepository.class);
  HabitRepository habits = mock(HabitRepository.class);
  CompletionRepository completions = mock(CompletionRepository.class);
  HabitService service = new HabitService(
    users,
    habits,
    completions,
    "America/Recife"
  );
  AppUser user = mock(AppUser.class);
  Habit habit = mock(Habit.class);

  @BeforeEach
  void setup() {
    when(users.findByEmail("u@example.com")).thenReturn(Optional.of(user));
    when(user.getId()).thenReturn(1L);
    when(habits.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(habit));
  }

  @Test
  void rejectsFutureDate() {
    assertThrows(ApiException.class, () ->
      service.mark(
        "u@example.com",
        3L,
        LocalDate.now(ZoneId.of("America/Recife")).plusDays(1)
      )
    );
    verify(completions, never()).insertIfAbsent(anyLong(), any());
  }

  @Test
  void refusesOtherUsersHabit() {
    when(habits.findByIdAndUserId(4L, 1L)).thenReturn(Optional.empty());
    assertEquals(
      404,
      assertThrows(ApiException.class, () -> service.get("u@example.com", 4L))
        .getStatus()
        .value()
    );
  }

  @Test
  void markingDelegatesToAtomicInsert() {
    LocalDate date = LocalDate.of(2020, 1, 1);
    service.mark("u@example.com", 3L, date);
    service.mark("u@example.com", 3L, date);
    verify(completions, times(2)).insertIfAbsent(3L, date);
    verify(completions, never()).findByHabitIdAndDate(anyLong(), any());
  }

  @Test
  void rejectsInvalidRange() {
    assertThrows(ApiException.class, () ->
      service.history(
        "u@example.com",
        3L,
        LocalDate.of(2025, 2, 1),
        LocalDate.of(2025, 1, 1)
      )
    );
  }

  @Test
  void archivedHabitCannotBeMarked() {
    when(habit.isArchived()).thenReturn(true);
    assertEquals(
      409,
      assertThrows(ApiException.class, () ->
        service.mark("u@example.com", 3L, LocalDate.of(2020, 1, 1))
      )
        .getStatus()
        .value()
    );
    verify(completions, never()).insertIfAbsent(anyLong(), any());
  }
}
