package com.sting.loginmvc.service;

import com.sting.loginmvc.dto.RegisterRequest;
import com.sting.loginmvc.models.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sting.loginmvc.repository.UserRepository;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public String register(RegisterRequest request){
    User user = new User(
      request.getName(),
      request.getUsername(),
      passwordEncoder.encode(request.getPassword())
    );

    userRepository.save(user);
    return "Usuario creado!";
  }
}
