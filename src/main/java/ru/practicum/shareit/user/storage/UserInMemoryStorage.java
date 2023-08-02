package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ObjectAlreadyExistsException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class UserInMemoryStorage implements UserStorage {
    private int generationId = 1;
    private final HashMap<Integer, User> users = new HashMap<>();

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        if (checkEmailAvailabilityInMemory(user.getId(), user.getEmail())) {
            user.setId(generationId++);
            users.put(user.getId(), user);
            return user;
        } else {
            throw new ObjectAlreadyExistsException(
                    String.format("Ошибка сохранения пользователя. Пользователь с такой почтой уже существует! Email=%s", user.getEmail()));
        }
    }

    @Override
    public User update(int id, UserUpdateDto user) {
        if (!users.containsKey(id)) {
            throw new ObjectNotFoundException(
                    String.format("Ошибка обновления пользователя. Пользователь не найден! Id=%d", id));
        } else {
            User tempUser = users.get(id);
            if (user.getName() != null && !user.getName().isBlank()) tempUser.setName(user.getName());
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                if (checkEmailAvailabilityInMemory(tempUser.getId(), user.getEmail()))
                    tempUser.setEmail(user.getEmail());
                else throw new ObjectAlreadyExistsException(
                        String.format("Ошибка обновления пользователя. Пользователь с таким email уже существует! Email=%s",
                                user.getEmail()));
            }
            users.put(id, tempUser);
            return tempUser;
        }
    }

    @Override
    public User getUserById(int userId) {
        if (users.containsKey(userId)) {
            return users.get(userId);
        } else {
            throw new ObjectNotFoundException(String.format("Пользователь не найден! Id=%d", userId));
        }
    }

    @Override
    public User deleteUserById(int userId) {
        if (users.containsKey(userId)) {
            return users.remove(userId);
        } else {
            throw new ObjectNotFoundException(String.format("Пользователь не найден! Id=%d", userId));
        }
    }

    private boolean checkEmailAvailabilityInMemory(int userId, String email) {
        return users.values().stream().noneMatch(userI -> userI.getEmail().equals(email) && userI.getId() != userId);
    }
}
