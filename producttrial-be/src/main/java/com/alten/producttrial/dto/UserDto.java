package com.alten.producttrial.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String username;

    private String firstname;

    private String email;

    private String password;
}
