package com.Sehaty.Sehaty.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http.csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers(
                        "api/users/register","api/users/login","api/users/update/**","api/users/{userId}",
                        "api/medical-files/upload/**","api/medical-files/user/**","api/medical-files/{fileId}",
                        "api/share/create/**","api/share/by-qr/**","api/share/revoke/**","api/share/access/**"
                ).permitAll()
                .anyRequest().authenticated();
        return http.build();

    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new Argon2PasswordEncoder(16,32,1,65536,3);
    }
}
