package br.com.habittracker.service;
import br.com.habittracker.dto.ApiDtos.*;
import br.com.habittracker.entity.*;
import br.com.habittracker.exception.ApiException;
import br.com.habittracker.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
@Service
@Transactional
public class HabitService {
 private final UserRepository users; private final HabitRepository habits; private final CompletionRepository completions; private final ZoneId zone;
 public HabitService(UserRepository users,HabitRepository habits,CompletionRepository completions,@Value("${app.timezone}") String zone){this.users=users;this.habits=habits;this.completions=completions;this.zone=ZoneId.of(zone);}
 private AppUser user(String email){return users.findByEmail(email).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"Usuário não encontrado."));}
 private Habit own(String email,Long id){return habits.findByIdAndUserId(id,user(email).getId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Hábito não encontrado."));}
 private HabitView view(Habit h){return new HabitView(h.getId(),h.getName(),h.getDescription(),h.isArchived());}
 private void active(Habit h){if(h.isArchived()) throw new ApiException(HttpStatus.CONFLICT,"Hábito arquivado não pode ser alterado.");}
 public List<HabitView> list(String email,boolean archived){return habits.findByUserIdAndArchivedOrderByIdDesc(user(email).getId(),archived).stream().map(this::view).toList();}
 public HabitView get(String email,Long id){return view(own(email,id));}
 public HabitView create(String email,HabitInput input){return view(habits.save(new Habit(user(email),input.name().trim(),input.description())));}
 public HabitView update(String email,Long id,HabitInput input){Habit h=own(email,id);active(h);h.update(input.name().trim(),input.description());return view(h);}
 public void archive(String email,Long id){Habit h=own(email,id);h.archive();}
 public void mark(String email,Long id,LocalDate date){Habit h=own(email,id);active(h);if(date.isAfter(LocalDate.now(zone))) throw new ApiException(HttpStatus.BAD_REQUEST,"Não é possível registrar datas futuras."); if(completions.findByHabitIdAndDate(id,date).isEmpty()) completions.save(new Completion(h,date));}
 public void unmark(String email,Long id,LocalDate date){Habit h=own(email,id);active(h);completions.findByHabitIdAndDate(id,date).ifPresent(completions::delete);}
 public History history(String email,Long id,LocalDate from,LocalDate to){own(email,id);if(from.isAfter(to)) throw new ApiException(HttpStatus.BAD_REQUEST,"Período inválido.");var dates=completions.findByHabitIdAndDateBetweenOrderByDateDesc(id,from,to).stream().map(Completion::getDate).toList();return new History(dates,dates.size());}
}
