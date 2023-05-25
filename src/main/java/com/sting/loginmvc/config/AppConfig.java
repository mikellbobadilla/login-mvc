package com.sting.loginmvc.config;

import com.sting.loginmvc.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
public class AppConfig {

  private final UserRepository userRepository;

  public AppConfig(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder(10);
  }

  /*
    Se utilizó el término de clase anónima, porque la clase 'UserDetailsService' solo tiene un método,
    que es 'loadByUsername()'
    Ejemplo:
    @Bean
    public UserDetailsService userDetailsService(){
      return new UserDetailsService() {
        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
          return userRepository.findByUsername(username)
                   .orElseThrow(()-> new UsernameNotFoundException("El usuario no existe!"));
        }
      };
    }
  */
  @Bean
  public UserDetailsService userDetailsService(){
    /*
      Se usó lambda para que sea más legible,
      pero puedes usar el ejemplo que está en el comentario;
      debería funcionar igual.
    */
    return username -> userRepository.findByUsername(username)
       .orElseThrow(()-> new UsernameNotFoundException("El usuario no existe!"));
  }

  /* El proveedor que usará Spring para la autenticación, se pueden agregar más tipos de autenticación */
  @Bean
  public AuthenticationProvider authProvider(){
    var provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService());
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authManager(AuthenticationConfiguration auth) throws Exception {
    return auth.getAuthenticationManager();
  }
}
