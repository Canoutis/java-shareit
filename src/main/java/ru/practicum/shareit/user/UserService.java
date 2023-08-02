package ru.practicum.shareit.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectAlreadyExistsException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<UserDto> findAll() {
        return userStorage.findAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    public UserDto create(UserDto userDto) {
        if (checkEmailAvailabilityInMemory(userDto.getId(), userDto.getEmail())) {
            return UserMapper.toUserDto(userStorage.create(UserMapper.toUserEntity(userDto)));
        } else {
            throw new ObjectAlreadyExistsException(
                    String.format("Ошибка сохранения пользователя. Пользователь с такой почтой уже существует! Email=%s", userDto.getEmail()));
        }
    }

    public UserDto update(int userId, UserDto userDto) {
        User user = userStorage.getUserById(userId);
        if (userDto.getName() != null && !userDto.getName().isBlank())
            user.setName(userDto.getName());
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            if (checkEmailAvailabilityInMemory(user.getId(), userDto.getEmail()))
                user.setEmail(userDto.getEmail());
            else throw new ObjectAlreadyExistsException(
                    String.format("Ошибка обновления пользователя. Пользователь с таким email уже существует! Email=%s",
                            userDto.getEmail()));
        }
        return UserMapper.toUserDto(userStorage.update(user));
    }

    public UserDto getUserById(int id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new ObjectNotFoundException(String.format("Пользователь не найден! Id=%d", id));
        }
        return UserMapper.toUserDto(user);
    }

    public UserDto deleteUserById(int id) {
        getUserById(id);
        return UserMapper.toUserDto(userStorage.deleteUserById(id));
    }

    public boolean checkEmailAvailabilityInMemory(int userId, String email) {
        return findAll().stream().noneMatch(userI -> userI.getEmail().equals(email) && userI.getId() != userId);
    }

}