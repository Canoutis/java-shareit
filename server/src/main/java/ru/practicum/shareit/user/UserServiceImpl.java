package ru.practicum.shareit.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.utils.Helper.findUserById;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUserEntity(userDto)));
    }

    @Override
    @Transactional
    public UserDto update(int userId, UserDto userDto) {
        User user = findUserById(userRepository, userId);
        if (userDto.getName() != null && !userDto.getName().isBlank())
            user.setName(userDto.getName());
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank() && !user.getEmail().equals(userDto.getEmail())) {
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getUserById(int id) {
        return UserMapper.toUserDto(findUserById(userRepository, id));
    }

    @Override
    @Transactional
    public void deleteUserById(int id) {
        getUserById(id);
        userRepository.deleteById(id);
    }
}