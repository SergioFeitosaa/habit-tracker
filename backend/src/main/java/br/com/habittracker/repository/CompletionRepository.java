package br.com.habittracker.repository;
import br.com.habittracker.entity.Completion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;
public interface CompletionRepository extends JpaRepository<Completion,Long> {
 Optional<Completion> findByHabitIdAndDate(Long id,LocalDate date);
 List<Completion> findByHabitIdAndDateBetweenOrderByDateDesc(Long id,LocalDate from,LocalDate to);
}
