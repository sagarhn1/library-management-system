package com.sagar.security.config;

import com.sagar.user.model.AuthProvider;
import com.sagar.user.model.Role;
import com.sagar.user.model.User;
import com.sagar.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createAdmin() {
        return args -> {

            if (userRepository.findByEmail("sagarhn1004@gmail.com").isEmpty()) {

                User admin = User.builder()
                        .firstName("Sagar")
                        .lastName("H N")
                        .email("sagarhn1004@gmail.com")
                        .phone("6363354367")
                        .password(passwordEncoder.encode("Sagarhn@2004"))
                        .role(Role.ADMIN)
                        .authProvider(AuthProvider.LOCAL)
                        .active(true)
                        .build();

                userRepository.save(admin);

                System.out.println("Admin user created successfully");
            }
        };
    }
}