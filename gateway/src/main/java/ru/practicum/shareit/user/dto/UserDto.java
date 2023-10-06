package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Validated
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Integer id;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String name;
}
