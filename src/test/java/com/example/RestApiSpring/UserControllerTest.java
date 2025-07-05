package com.example.RestApiSpring;

import com.example.RestApiSpring.model.User;
import com.example.RestApiSpring.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private  User user2;

    private List<User> manyUsers;
//    private long initialUserCount;

    @BeforeEach
    public void setUp(){
        userRepository.deleteAll();
        objectMapper.registerModule(new JavaTimeModule());

        User initialUser1 = new User("John Doe","john.doe@example.com");
        User initialUser2 = new User("vikash patel","vikash.patel@example.com");

        List<User> savedInitialUsers = userRepository.saveAll(Arrays.asList(initialUser1,initialUser2));

        this.user1 =savedInitialUsers.get(0);
        this.user2 =savedInitialUsers.get(1);


        manyUsers = IntStream.rangeClosed(1,25).mapToObj(i-> new User("User"+i,"user"+i+"@example.com")).collect(Collectors.toList());
        userRepository.saveAll(manyUsers);

    }
//    Arrange-Act-Assert (AAA) pattern;

    @Test
    public void testGetUserByIdFound() throws Exception{
        mockMvc.perform(get("/api/users/{id}",user1.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user1.getId()))
                .andExpect(jsonPath("$.name").value(user1.getName()))
                .andExpect(jsonPath("$.email").value(user1.getEmail()));
    }
}

