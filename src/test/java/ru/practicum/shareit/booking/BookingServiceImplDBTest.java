package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImplDBTest {

    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    @Test
    void testFindBookingsByItemsOwnerAll() {

        UserDto userDto1 = new UserDto(null, "test@etcdev.ru", "Test Test");
        UserDto userDto2 = new UserDto(null, "test2@etcdev.ru", "Test2 Test2");
        UserDto userDto3 = new UserDto(null, "test3@etcdev.ru", "Test3 Test3");
        UserDto createdUser1 = userService.create(userDto1);
        UserDto createdUser2 = userService.create(userDto2);
        UserDto createdUser3 = userService.create(userDto3);

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

        ItemDto createdItem1 = itemService.create(createdUser1.getId(), itemDto1);
        ItemDto createdItem2 = itemService.create(createdUser1.getId(), itemDto2);

        BookItemRequestDto bookItemRequestDto1 = new BookItemRequestDto(createdItem1.getId(),
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(10),
                createdItem1.getId(),
                createdUser2.getId()
        );
        bookItemRequestDto1.setItemId(createdItem1.getId());
        bookItemRequestDto1.setStart(LocalDateTime.now().plusDays(5));
        bookItemRequestDto1.setEnd(LocalDateTime.now().plusDays(10));
        BookingDto createdBooking1 = bookingService.create(bookItemRequestDto1, createdUser2.getId());

        bookingService.approve(createdBooking1.getId(), createdUser1.getId(), true);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.item.owner.id = :id", Booking.class);
        List<Booking> bookings = query.setParameter("id", createdUser1.getId())
                .getResultList();

        assertThat(bookings, notNullValue());
        assertThat(bookings.size(), equalTo(1));

        BookItemRequestDto bookItemRequestDto2 = new BookItemRequestDto(createdItem1.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                createdItem2.getId(),
                createdUser2.getId()
        );
        BookingDto createdBooking2 = bookingService.create(bookItemRequestDto2, createdUser3.getId());
        bookingService.approve(createdBooking2.getId(), createdUser1.getId(), true);

        query = em.createQuery("Select b from Booking b where b.item.owner.id = :id", Booking.class);
        bookings = query.setParameter("id", createdUser1.getId())
                .getResultList();

        assertThat(bookings, notNullValue());
        assertThat(bookings.size(), equalTo(2));

    }
}
