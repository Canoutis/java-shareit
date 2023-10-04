package ru.practicum.shareit.utils;

import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Optional;

public class Helper {
    public static User findUserById(UserRepository repository, Integer id) {
        Optional<User> user = repository.findById(id);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException(String.format("Пользователь не найден! Id=%d", id));
        }
        return user.get();
    }
}
