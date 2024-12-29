package com.alten.producttrial.service;

import com.alten.producttrial.dto.LoginRequest;
import com.alten.producttrial.dto.UserDto;
import com.alten.producttrial.entity.User;
import com.alten.producttrial.exception.InvalidCredentialsException;
import com.alten.producttrial.exception.UserAlreadyExistsException;
import com.alten.producttrial.exception.UserNotFoundException;
import com.alten.producttrial.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new user account.
     *
     * @param userDto
     * @return the created user entity
     * @throws UserAlreadyExistsException if the email is already taken
     */
    public User createAccount(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("A user with this email already exists.");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setFirstname(userDto.getFirstname());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        return userRepository.save(user);
    }

    /**
     * Authenticates the user based on email and password.
     *
     * @param loginRequest the login request containing the user's credentials
     * @return the authenticated user entity
     * @throws UserNotFoundException       if no user is found with the given email
     * @throws InvalidCredentialsException if the password is incorrect
     */
    public User authenticate(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + loginRequest.getEmail()));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials provided.");
        }

        return user;
    }
}
