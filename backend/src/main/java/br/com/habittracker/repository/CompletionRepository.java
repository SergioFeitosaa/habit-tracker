package br.com.habittracker.repository;

import br.com.habittracker.entity.Completion;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletionRepository extends JpaRepository<Completion, Long> {
  Optional<Completion> findByHabitIdAndDate(Long id, LocalDate date);
  List<Completion> findByHabitIdAndDateBetweenOrderByDateDesc(
    Long id,
    LocalDate from,
    LocalDate to
  );
}
