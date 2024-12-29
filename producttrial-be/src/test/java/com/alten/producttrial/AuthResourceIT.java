package com.alten.producttrial;

import com.alten.producttrial.dto.LoginRequest;
import com.alten.producttrial.dto.UserDto;
import com.alten.producttrial.entity.User;
import com.alten.producttrial.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabaseBeforeTest() {
        userRepository.deleteAll();
    }

    private UserDto createValidUserDto() {
        return UserDto.builder()
                .username("testuser")
                .firstname("Test")
                .email("testuser@example.com")
                .password("password123")
                .build();
    }

    private LoginRequest createValidLoginRequest() {
        return LoginRequest.builder()
                .email("testuser@example.com")
                .password("password123")
                .build();
    }

    @Test
    void shouldCreateAccountSuccessfully() throws Exception {
        // Arrange
        UserDto userDto = createValidUserDto();

        // Act & Assert
        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(userDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User created successfully"));

        // Verify user is created in the database
        Optional<User> user = userRepository.findByEmail(userDto.getEmail());
        assert (user.isPresent());
    }

    @Test
    void shouldReturnConflictWhenUserAlreadyExist() throws Exception {
        // Arrange
        UserDto userDto = createValidUserDto();
        User user = new User();
        user.setFirstname(userDto.getFirstname());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        userRepository.save(user);

        // Act & Assert
        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(userDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("A user with this email already exists.")));
    }

    @Test
    void shouldAuthenticateUserSuccessfully() throws Exception {
        // Arrange
        UserDto userDto = createValidUserDto();
        User user = new User();
        user.setFirstname(userDto.getFirstname());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        userRepository.save(user);

        LoginRequest loginRequest = createValidLoginRequest();

        // Act & Assert
        mockMvc.perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(notNullValue()))); // Assert that token is returned
    }

    @Test
    void shouldReturnBadRequestWhenInvalidCredentials() throws Exception {
        // Arrange
        User user = new User();
        user.setFirstname("Test");
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("testuser@example.com")
                .password("wrongpassword")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid credentials provided.")));
    }

    @Test
    void shouldReturnNotFoundWhenUserNotExistForLogin() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(loginRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("User not found with email: nonexistent@example.com")));
    }
}
