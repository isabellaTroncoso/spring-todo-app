package org.example.todoapp.todos;
import org.example.todoapp.user.custom.CustomUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public Todo createTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    public Optional<Todo> getTodoById(UUID id) {
        return todoRepository.findById(id);
    }

    public List<Todo> getTodosByUser(CustomUser user) {
        return todoRepository.findAllByUser(user);
    }

    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public void deleteTodo(UUID id) {
        todoRepository.deleteById(id);
    }
}