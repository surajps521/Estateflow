package com.realestate.crm;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
@SpringBootApplication
@EnableJpaAuditing
@EnableMethodSecurity
public class CrmApplication {
  public static void main(String[] args) { SpringApplication.run(CrmApplication.class, args); }
}
