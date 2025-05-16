package ru.diszexuf.streamlive.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.diszexuf.streamlive.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtFilter;
  private final AuthenticationProvider authenticationProvider;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/users").permitAll() // POST для регистрации разрешен всем
            .requestMatchers("/api/streams").permitAll() // GET для получения всех потоков разрешен всем
            .requestMatchers("/api/streams/live").permitAll() // GET для получения активных потоков разрешен всем
            .requestMatchers("/api/streams/*/search").permitAll() // GET для поиска потоков разрешен всем
            .requestMatchers("/api/streams/user/*").permitAll() // GET для получения потоков пользователя разрешен всем
            .requestMatchers(req -> req.getMethod().equals("GET") && req.getRequestURI().matches("/api/streams/[^/]+")).permitAll() // GET для получения потока по ID разрешен всем
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

}
