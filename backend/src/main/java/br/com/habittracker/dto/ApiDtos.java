package br.com.habittracker.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public final class ApiDtos {

  private ApiDtos() {}

  public record Register(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(min = 8, max = 72) String password
  ) {}

  public record Login(
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(max = 72) String password
  ) {}

  public record Token(String token, String name) {}

  public record HabitInput(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String description
  ) {}

  public record HabitView(
    Long id,
    String name,
    String description,
    boolean archived
  ) {}

  public record History(List<LocalDate> dates, int total) {}

  public record ApiError(String message) {}
}
