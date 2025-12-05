package com.logitrack.b2b_tradehub.controller;

import com.logitrack.b2b_tradehub.entity.User;
import com.logitrack.b2b_tradehub.entity.enums.UserRole;
import com.logitrack.b2b_tradehub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable UserRole role) {
        List<User> users = userService.findByRole(role);
        return ResponseEntity.ok(users);
    }
}