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

    @Bean
    public PasswordEncoder defaultPasswordEncoder() {

        /* Implementation of PasswordEncoder that uses the BCrypt strong hashing function.
        Clients can optionally supply a "version" ($2a, $2b, $2y) and
        a "strength" (a. k. a. log rounds in BCrypt) and a SecureRandom instance.

        The larger the strength parameter the more work will have to be done (exponentially)
        to hash the passwords. The default value is 10. */

        /* Iterations / Strength
         *   More iterations == More Secure
         *   Higher Value == Higher CPU Cost
         *   NOTE: Can lead to potential Bottleneck (Performance Issues)
         * */

        return new BCryptPasswordEncoder(12); // Default Strength 10 (Iterations)
    }

}

