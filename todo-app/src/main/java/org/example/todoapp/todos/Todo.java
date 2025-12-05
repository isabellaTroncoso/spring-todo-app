package org.example.todoapp.todos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.example.todoapp.user.custom.CustomUser;

import java.util.UUID;

@Entity
@Table(name = "todos")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    private boolean completed;

    // Many todos can belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private CustomUser user;

    public Todo() {}

    public Todo(String title, String description, boolean completed, CustomUser user) {
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.user = user;
    }

    // Getters & setters
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public CustomUser getUser() { return user; }
    public void setUser(CustomUser user) { this.user = user; }
}
