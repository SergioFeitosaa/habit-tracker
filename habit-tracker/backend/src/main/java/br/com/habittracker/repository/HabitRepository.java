package br.com.habittracker.repository;
import br.com.habittracker.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface HabitRepository extends JpaRepository<Habit,Long> {
 List<Habit> findByUserIdAndArchivedOrderByIdDesc(Long userId,boolean archived);
 Optional<Habit> findByIdAndUserId(Long id,Long userId);
}
