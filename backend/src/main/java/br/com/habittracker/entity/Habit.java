package br.com.habittracker.entity;
import jakarta.persistence.*;
@Entity @Table(name="habits")
public class Habit {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private AppUser user;
 @Column(nullable=false,length=100) private String name;
 @Column(length=500) private String description;
 @Column(nullable=false) private boolean archived=false;
 protected Habit() {}
 public Habit(AppUser user,String name,String description){this.user=user;this.name=name;this.description=description;}
 public Long getId(){return id;} public String getName(){return name;} public String getDescription(){return description;} public boolean isArchived(){return archived;}
 public void update(String name,String description){this.name=name;this.description=description;} public void archive(){this.archived=true;}
}
