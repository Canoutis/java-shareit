package ru.practicum.shareit.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(int id, UserUpdateDto userUpdateDto) {
        return userStorage.update(id, userUpdateDto);
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    public User deleteUserById(int id) {
        return userStorage.deleteUserById(id);
    }
}