package br.com.habittracker.repository;

import br.com.habittracker.entity.Habit;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository extends JpaRepository<Habit, Long> {
  List<Habit> findByUserIdAndArchivedOrderByIdDesc(
    Long userId,
    boolean archived
  );
  Optional<Habit> findByIdAndUserId(Long id, Long userId);
}
