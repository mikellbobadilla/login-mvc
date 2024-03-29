package com.sting.loginmvc.controllers;

import com.sting.loginmvc.models.User;
import com.sting.loginmvc.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class IndexController {
  private final UserRepository userRepository;

  public IndexController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/")
  public String indexPage(Model model){
    List<User> users = userRepository.findAll();
    model.addAttribute("users", users);
    return "index";
  }
}
