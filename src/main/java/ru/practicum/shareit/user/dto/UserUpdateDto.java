package ru.practicum.shareit.user.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class UserUpdateDto {
    private String email;
    private String name;
}
