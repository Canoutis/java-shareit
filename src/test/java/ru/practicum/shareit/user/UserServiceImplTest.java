package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith({MockitoExtension.class})
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testFindAllOk() {
        UserDto userDto = new UserDto(1, "test@etcdev.ru", "Test Test");

        Mockito.when(userRepository.findAll())
                .thenReturn(Collections.singletonList(UserMapper.toUserEntity(userDto)));
        userService.findAll();

        Mockito.verify(userRepository, Mockito.times(1))
                .findAll();

    }

    @Test
    void testCreateUserOk() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test@etcdev.ru");
        userDto.setName("Test Test");

        Mockito.when(userRepository.save(any(User.class)))
                .thenReturn(UserMapper.toUserEntity(userDto));
        userService.create(userDto);

        Mockito.verify(userRepository, Mockito.times(1))
                .save(any(User.class));

    }

    @Test
    void testDeleteUserOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        userService.deleteUserById(1);

        Mockito.verify(userRepository, Mockito.times(1))
                .deleteById(1);

    }

    @Test
    void testGetUserByIdOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        UserDto userDto = userService.getUserById(1);

        Assertions.assertEquals(user.getId(), userDto.getId());
        Assertions.assertEquals(user.getName(), userDto.getName());
        Assertions.assertEquals(user.getEmail(), userDto.getEmail());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);

    }

    @Test
    void testGetUserByIdThrowsException() {
        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(
                ObjectNotFoundException.class,
                () -> userService.getUserById(1));

        Assertions.assertEquals("Пользователь не найден! Id=1", exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);

    }

    @Test
    void testUpdateUserOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");

        UserDto toUpdateUserDto = new UserDto();
        toUpdateUserDto.setEmail("updated@etcdev.ru");
        toUpdateUserDto.setName("Test Updated");

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class)))
                .thenReturn(UserMapper.toUserEntity(toUpdateUserDto));

        UserDto updatedUserDto = userService.update(1, toUpdateUserDto);

        Assertions.assertEquals(toUpdateUserDto.getId(), updatedUserDto.getId());
        Assertions.assertEquals(toUpdateUserDto.getName(), updatedUserDto.getName());
        Assertions.assertEquals(toUpdateUserDto.getEmail(), updatedUserDto.getEmail());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(userRepository, Mockito.times(1))
                .save(any(User.class));

    }

    @Test
    void testUpdateUserOnlyNameOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");

        UserDto toUpdateUserDto = new UserDto();
        toUpdateUserDto.setName("Test Updated");

        UserDto returnUserDto = UserMapper.toUserDto(user);
        returnUserDto.setName("Test Updated");


        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class)))
                .thenReturn(UserMapper.toUserEntity(returnUserDto));

        UserDto updatedUserDto = userService.update(1, toUpdateUserDto);

        Assertions.assertEquals(returnUserDto.getId(), updatedUserDto.getId());
        Assertions.assertEquals(returnUserDto.getName(), updatedUserDto.getName());
        Assertions.assertEquals(returnUserDto.getEmail(), updatedUserDto.getEmail());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(userRepository, Mockito.times(1))
                .save(any(User.class));

    }

    @Test
    void testUpdateUserOnlyEmailOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");

        UserDto toUpdateUserDto = new UserDto();
        toUpdateUserDto.setEmail("updated@etcdev.ru");

        UserDto returnUserDto = UserMapper.toUserDto(user);
        returnUserDto.setEmail("updated@etcdev.ru");


        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class)))
                .thenReturn(UserMapper.toUserEntity(returnUserDto));

        UserDto updatedUserDto = userService.update(1, toUpdateUserDto);

        Assertions.assertEquals(returnUserDto.getId(), updatedUserDto.getId());
        Assertions.assertEquals(returnUserDto.getName(), updatedUserDto.getName());
        Assertions.assertEquals(returnUserDto.getEmail(), updatedUserDto.getEmail());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(userRepository, Mockito.times(1))
                .save(any(User.class));

    }

    @Test
    void testUpdateUserWithoutChangesOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserDto updatedUserDto = userService.update(1, UserMapper.toUserDto(user));

        Assertions.assertEquals(user.getId(), updatedUserDto.getId());
        Assertions.assertEquals(user.getName(), updatedUserDto.getName());
        Assertions.assertEquals(user.getEmail(), updatedUserDto.getEmail());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(userRepository, Mockito.times(1))
                .save(any(User.class));

    }

    @Test
    void testUpdateUserEmptyChangesOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserDto updatedUserDto = userService.update(1, new UserDto());

        Assertions.assertEquals(user.getId(), updatedUserDto.getId());
        Assertions.assertEquals(user.getName(), updatedUserDto.getName());
        Assertions.assertEquals(user.getEmail(), updatedUserDto.getEmail());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(userRepository, Mockito.times(1))
                .save(any(User.class));

    }
}
