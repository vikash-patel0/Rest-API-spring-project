package com.example.RestApiSpring.controller;


import com.example.RestApiSpring.model.User;
import com.example.RestApiSpring.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
public class UserController {

//    @Autowired   // Injecting the UserRepository
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Collections.singleton("ROLE_USER"));
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }


    @PostMapping("/batch") //this endpoint is now protected by hasRole("ADMIN")
    public ResponseEntity<List<User>> createUsers(@Valid @RequestBody List<User> users) {
        // Encode passwords and assign roles for each user in the list
        users.forEach(user-> {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            //default role, or expect roles in request body if ADMIN can specify roles
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                user.setRoles(Collections.singleton("ROLE_USER"));
            }
        });
        List<User> savedUsers = userRepository.saveAll(users);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUsers);
    }

    @GetMapping  //this endpoint is now protected by hasRole("ADMIN")
    public List<User> getAllUsers() {
        // Retrieve all users from the database
        return userRepository.findAll();
    }

    @GetMapping("/page")
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @GetMapping("/{id}") //protected by hasAnyRole("USER","ADMIN")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        // Find the user by ID
        Optional<User> user = userRepository.findById(id);
        // If user is found, return it; otherwise, return 404 Not Found
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


    @PutMapping("/{id}") //protected by hasRole("USER","ADMIN")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setName(userDetails.getName());
                    existingUser.setEmail(userDetails.getEmail());

                    if(userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    }
                    if(userDetails.getRoles()!=null && !userDetails.getRoles().isEmpty()){
                        existingUser.setRoles(userDetails.getRoles());
                    }

                    // Optionally update other fields as needed
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
//            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        // Check if user with the email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email is already in use!"); //409 conflict if email exists
        }
        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Collections.singleton("ROLE_USER"));
        }
        // Save the new user
        User savedUser = userRepository.save(user);

        // Return the created user with HTTP 201 status
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);
    }


}