package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.storage.UserRepository;

import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
@Transactional
public class ItemRequestServiceImplDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    void testFindOtherItemRequests() {
        UserDto userDto1 = new UserDto(null, "test@etcdev.ru", "Test Test");
        UserDto userDto2 = new UserDto(null, "test2@etcdev.ru", "Test2 Test2");
        UserDto userDto3 = new UserDto(null, "test3@etcdev.ru", "Test3 Test3");
        User user1 = entityManager.persist(UserMapper.toUserEntity(userDto1));
        User user2 = entityManager.persist(UserMapper.toUserEntity(userDto2));
        User user3 = entityManager.persist(UserMapper.toUserEntity(userDto3));

        ItemRequestDto itemRequestDto1 = ItemRequestDto.builder()
                .description("Нужен мощный лобзик!")
                .created(LocalDateTime.now())
                .build();

        ItemRequestDto itemRequestDto2 = ItemRequestDto.builder()
                .description("Нужен мощный лобзик!")
                .created(LocalDateTime.now())
                .build();

        ItemRequestDto itemRequestDto3 = ItemRequestDto.builder()
                .description("Нужен мощный лобзик!")
                .created(LocalDateTime.now())
                .build();

        ItemRequestDto itemRequestDto4 = ItemRequestDto.builder()
                .description("Нужен мощный лобзик!")
                .created(LocalDateTime.now())
                .build();

        entityManager.persist(ItemRequestMapper.toItemRequestEntity(itemRequestDto1, user1));

        TypedQuery<ItemRequest> query = entityManager.getEntityManager()
                .createQuery("Select i from ItemRequest i where i.owner.id != :id", ItemRequest.class);
        List<ItemRequest> itemRequests = query.setParameter("id", user1.getId())
                .getResultList();

        assertThat(itemRequests.size(), equalTo(0));

        entityManager.persist(ItemRequestMapper.toItemRequestEntity(itemRequestDto2, user2));

        query = entityManager.getEntityManager()
                .createQuery("Select i from ItemRequest i where i.owner.id != :id", ItemRequest.class);
        itemRequests = query.setParameter("id", user1.getId())
                .getResultList();

        assertThat(itemRequests.size(), equalTo(1));

        entityManager.persist(ItemRequestMapper.toItemRequestEntity(itemRequestDto3, user3));

        query = entityManager.getEntityManager()
                .createQuery("Select i from ItemRequest i where i.owner.id != :id", ItemRequest.class);
        itemRequests = query.setParameter("id", user1.getId())
                .getResultList();

        assertThat(itemRequests.size(), equalTo(2));

        entityManager.persist(ItemRequestMapper.toItemRequestEntity(itemRequestDto4, user1));

        query = entityManager.getEntityManager()
                .createQuery("Select i from ItemRequest i where i.owner.id != :id", ItemRequest.class);
        itemRequests = query.setParameter("id", user1.getId())
                .getResultList();

        assertThat(itemRequests.size(), equalTo(2));

    }
}
