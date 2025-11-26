package org.example.todoapp.todos;

import org.example.todoapp.user.custom.CustomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TodoRepository extends JpaRepository<Todo, UUID> {
    List<Todo> findByUser(CustomUser user);
}
