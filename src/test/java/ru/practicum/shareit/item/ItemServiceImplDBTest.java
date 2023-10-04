package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplDBTest {

    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;

    @Test
    void testGetItemsByOwnerId() {
        UserDto userDto = new UserDto(null, "test@etcdev.ru", "Test Test");
        UserDto createdUser = userService.create(userDto);

        ItemDto itemDto1 = ItemDto.builder()
                .name("Перфоратор")
                .description("Электрический")
                .available(true)
                .build();
        ItemDto itemDto2 = ItemDto.builder()
                .name("Перфоратор2")
                .description("Электрический2")
                .available(true)
                .build();

        itemService.create(createdUser.getId(), itemDto1);
        itemService.create(createdUser.getId(), itemDto2);

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.owner.id = :id", Item.class);
        List<Item> items = query.setParameter("id", createdUser.getId())
                .getResultList();

        assertThat(items, notNullValue());
        assertThat(items.size(), equalTo(2));

    }
}
