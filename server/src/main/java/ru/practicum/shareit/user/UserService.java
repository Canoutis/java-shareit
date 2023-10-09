package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    List<UserDto> findAll();

    UserDto create(UserDto userDto);

    UserDto update(int userId, UserDto userDto);

    UserDto getUserById(int id);

    void deleteUserById(int id);
}
