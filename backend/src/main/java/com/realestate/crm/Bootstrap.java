package com.realestate.crm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
class Bootstrap {
 @Bean CommandLineRunner createInitialAdmin(UserRepository users,PasswordEncoder encoder,
   @Value("${app.bootstrap.admin-email}") String email,
   @Value("${app.bootstrap.admin-password}") String password) {
   return args -> { if(users.findByEmail(email).isEmpty()) users.save(new User(email,encoder.encode(password),Role.ADMIN)); };
 }
}
