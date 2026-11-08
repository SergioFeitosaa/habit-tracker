package br.com.habittracker.repository;

import br.com.habittracker.entity.Habit;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface HabitRepository extends JpaRepository<Habit, Long> {
  List<Habit> findByUserIdAndArchivedOrderByIdDesc(
    Long userId,
    boolean archived
  );
  Optional<Habit> findByIdAndUserId(Long id, Long userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Habit> findWithLockByIdAndUserId(Long id, Long userId);
}
