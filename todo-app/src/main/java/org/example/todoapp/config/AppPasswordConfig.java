package org.example.todoapp.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/* PasswordEncoder bean: används för att hash lösenord med BCrypt
 Detta gör att lösenord sparas säkert i databasen */

@Configuration
public class AppPasswordConfig {

    /* Password Hashing: Encoder
     *   The Encoder == password hashing (Generic Term)
     *   Class Abstraction to be used == PasswordEncoder
     * */

}
