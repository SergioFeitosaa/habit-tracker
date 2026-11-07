package br.com.habittracker.controller;

import br.com.habittracker.dto.ApiDtos.*;
import br.com.habittracker.service.HabitService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

  private final HabitService service;

  public HabitController(HabitService service) {
    this.service = service;
  }

  @GetMapping
  public List<HabitView> list(
    Principal p,
    @RequestParam(defaultValue = "false") boolean archived
  ) {
    return service.list(p.getName(), archived);
  }

  @GetMapping("/{id}")
  public HabitView get(Principal p, @PathVariable Long id) {
    return service.get(p.getName(), id);
  }

  @PostMapping
  public ResponseEntity<HabitView> create(
    Principal p,
    @Valid @RequestBody HabitInput input
  ) {
    return ResponseEntity.status(201).body(service.create(p.getName(), input));
  }

  @PutMapping("/{id}")
  public HabitView update(
    Principal p,
    @PathVariable Long id,
    @Valid @RequestBody HabitInput input
  ) {
    return service.update(p.getName(), id, input);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> archive(Principal p, @PathVariable Long id) {
    service.archive(p.getName(), id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/completions/{date}")
  public ResponseEntity<Void> mark(
    Principal p,
    @PathVariable Long id,
    @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    service.mark(p.getName(), id, date);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/completions/{date}")
  public ResponseEntity<Void> unmark(
    Principal p,
    @PathVariable Long id,
    @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    service.unmark(p.getName(), id, date);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/completions")
  public History history(
    Principal p,
    @PathVariable Long id,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
  ) {
    return service.history(p.getName(), id, from, to);
  }
}
