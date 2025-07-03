package com.example.RestApiSpring.controller;


import com.example.RestApiSpring.model.User;
import com.example.RestApiSpring.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired   // Injecting the UserRepository
    private UserRepository userRepository;

//    @PostMapping
//    public ResponseEntity<User> createUser(@Valid @RequestBody User user){
//        // Save the user to the database
//        User savedUser = userRepository.save(user);
//
//        // Return the saved user with a 201 Created status
//        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
//    }

    @PostMapping("/batch")
    public ResponseEntity<List<User>> createUsers(@Valid @RequestBody List<User> users) {
        List<User> savedUsers = userRepository.saveAll(users);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUsers);
    }


    @GetMapping
    public List<User> getAllUsers() {
        // Retrieve all users from the database
        return userRepository.findAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        // Find the user by ID
        Optional<User> user = userRepository.findById(id);
        // If user is found, return it; otherwise, return 404 Not Found
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        // Find the user by ID
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setName(userDetails.getName());
                    existingUser.setEmail(userDetails.getEmail());
                    User updatedUser = userRepository.save(existingUser);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if(userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build(); // Return 204 No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return 404 Not Found
        }
    }
}

