package br.com.habittracker.repository;

import br.com.habittracker.entity.Completion;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompletionRepository extends JpaRepository<Completion, Long> {
  @Modifying
  @Query(
    value = "INSERT INTO completions (habit_id, completion_date) VALUES (:habitId, :date) ON CONFLICT (habit_id, completion_date) DO NOTHING",
    nativeQuery = true
  )
  int insertIfAbsent(
    @Param("habitId") Long habitId,
    @Param("date") LocalDate date
  );

  Optional<Completion> findByHabitIdAndDate(Long id, LocalDate date);
  List<Completion> findByHabitIdAndDateBetweenOrderByDateDesc(
    Long id,
    LocalDate from,
    LocalDate to
  );
}
