package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

public interface UserStorage {
    List<User> findAll();

    User create(User user);

    User update(int id, UserUpdateDto userUpdateDto);

    User getUserById(int id);

    User deleteUserById(int id);
}
