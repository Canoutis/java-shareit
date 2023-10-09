package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplDBTest {

    private final EntityManager em;
    private final UserService userService;

    @Test
    void testCreateUserOk() {
        UserDto userDto = new UserDto(null, "test@etcdev.ru", "Test Test");
        userService.create(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
        assertThat(user.getName(), equalTo(userDto.getName()));
    }

    @Test
    void testUpdateUserOk() {
        UserDto userDto = new UserDto(null, "test@etcdev.ru", "Test Test");
        UserDto createdUserDto = userService.create(userDto);
        userDto.setEmail("update@etcdev.ru");
        userDto.setName("Updated name");
        UserDto updatedUserDto = userService.update(createdUserDto.getId(), userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", updatedUserDto.getEmail())
                .getSingleResult();

        assertThat(user.getId(), equalTo(createdUserDto.getId()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
        assertThat(user.getName(), equalTo(userDto.getName()));
    }
}
