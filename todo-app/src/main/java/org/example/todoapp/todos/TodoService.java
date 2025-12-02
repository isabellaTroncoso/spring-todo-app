package org.example.todoapp.todos;
import org.example.todoapp.user.authority.UserRole;
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

    public List<Todo> getTodosForUser(CustomUser user) {
        return todoRepository.findByUser(user);
    }

    public Optional<Todo> getTodoById(UUID id) {
        return todoRepository.findById(id);
    }

    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public Todo createTodo(Todo todo) {
        return todoRepository.save(todo);
    }


    public Todo toggleTodoCompleted(UUID id, CustomUser user) {
        Todo todo = todoRepository.findById(id).orElse(null);
        if (todo == null) return null;

        if (!todo.getUser().getId().equals(user.getId()) &&
                !user.isAdmin()) {
            return null;
        }

        todo.setCompleted(!todo.isCompleted());
        return todoRepository.save(todo);
    }


    public void deleteTodoIfAllowed(UUID id, CustomUser user) {

        Todo todo = todoRepository.findById(id).orElse(null);
        if (todo == null) return;

        if (!todo.getUser().getId().equals(user.getId()) &&
                !user.getRoles().contains(UserRole.ADMIN)) {
            return;
        }

        todoRepository.deleteById(id);
    }


    public Todo updateTodoIfAllowed(UUID id, Todo form, CustomUser user) {
        Todo todo = todoRepository.findById(id).orElse(null);
        if (todo == null) return null;

        if (!todo.getUser().getId().equals(user.getId()) &&
                !user.getRoles().contains(UserRole.ADMIN)) {
            return null;
        }

        todo.setTitle(form.getTitle());
        todo.setDescription(form.getDescription());
        todo.setCompleted(form.isCompleted());

        return todoRepository.save(todo);
    }
    public Todo save(Todo todo) {
        return todoRepository.save(todo);
    }

}
