package com.sting.loginmvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final AuthenticationProvider authProvider;

  public SecurityConfig(AuthenticationProvider authProvider) {
    this.authProvider = authProvider;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
      .formLogin()
      /* Referencia al controlador que responde el formulario de login */
      .loginPage("/login")
      .and()
      /* El tipo de autenticación se configuró. Está dentro de la clase 'AppConfig' */
      .authenticationProvider(authProvider)
      .authorizeHttpRequests()
      /* Url que se puede acceder sin estar autenticado */
      .antMatchers("/login", "/register")
      .permitAll()
      /* El resto de los endpoints requiere autenticación */
      .anyRequest()
      .authenticated();


    return http.build();
  }
}
