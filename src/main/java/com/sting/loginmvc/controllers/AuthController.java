package com.sting.loginmvc.controllers;

import com.sting.loginmvc.dto.AuthRequest;
import com.sting.loginmvc.dto.RegisterRequest;
import com.sting.loginmvc.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.logging.Logger;

@Controller
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/login")
  public String loginPage(Model model){
    AuthRequest auth = new AuthRequest();
    model.addAttribute("authRequest", auth);
    return "login";
  }

  @GetMapping("/register")
  public String registerPage(Model model){
    RegisterRequest register = new RegisterRequest();
    model.addAttribute("registerRequest", register);
    return "register";
  }

  @PostMapping("/register")
  public String register(@ModelAttribute RegisterRequest request, RedirectAttributes attributes){
    String message = authService.register(request);
    attributes.addFlashAttribute("message", message);
    return "redirect:/login";
  }

  /* Manejo de Error, si el usuario ya está registrado */
  @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
  public String errorRegister(RedirectAttributes attributes, SQLIntegrityConstraintViolationException exc){
    Logger.getLogger(AuthController.class.getName()).warning(exc.getMessage());
    attributes.addFlashAttribute("message", "El usuario ya existe");
    return "redirect:/register";
  }
}
