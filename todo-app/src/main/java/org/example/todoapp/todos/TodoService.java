package org.example.todoapp.todos;
import org.example.todoapp.user.custom.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    @Autowired
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    // Hämta alla todos för en user
    public List<Todo> getTodosForUser(CustomUser user) {
        return todoRepository.findByUser(user);
    }

    // Hämta alla todos (Admin)
    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    // Hämta todo by id
    public Optional<Todo> getTodoById(UUID id) {
        return todoRepository.findById(id);
    }

    // Skapa todo
    public Todo createTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    // Uppdatera todo
    public Todo updateTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    // Radera todo
    public void deleteTodo(UUID id) {
        todoRepository.deleteById(id);
    }
}
