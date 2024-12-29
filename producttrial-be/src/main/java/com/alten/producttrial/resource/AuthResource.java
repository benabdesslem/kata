package com.alten.producttrial.resource;

import com.alten.producttrial.config.JwtTokenProvider;
import com.alten.producttrial.dto.LoginRequest;
import com.alten.producttrial.dto.TokenResponse;
import com.alten.producttrial.dto.UserDto;
import com.alten.producttrial.entity.User;
import com.alten.producttrial.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class AuthResource {

    private UserService userService;

    private JwtTokenProvider jwtTokenProvider;

    @Operation(
            summary = "Create a new user account",
            description = "Allows a new user to create an account by providing their details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping("/account")
    public ResponseEntity<?> createAccount(@RequestBody UserDto userDto) {
        userService.createAccount(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
    }

    @Operation(
            summary = "Authenticate a user",
            description = "Allows a user to authenticate by providing their email and password."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })

    @PostMapping("/token")
    public ResponseEntity<?> authenticate(@RequestBody LoginRequest loginRequest) {
        User user = userService.authenticate(loginRequest);
        String token = jwtTokenProvider.generateToken(user.getEmail());
        return ResponseEntity.ok(new TokenResponse(token));
    }
}
