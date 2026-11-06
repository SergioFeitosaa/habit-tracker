package br.com.habittracker.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name="completions",uniqueConstraints=@UniqueConstraint(columnNames={"habit_id","completion_date"}))
public class Completion {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="habit_id",nullable=false) private Habit habit;
 @Column(name="completion_date",nullable=false) private LocalDate date;
 protected Completion() {} public Completion(Habit habit,LocalDate date){this.habit=habit;this.date=date;}
 public LocalDate getDate(){return date;}
}
