package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void testBookingDto() throws Exception {
        UserDto bookerDto = new UserDto(
                2,
                "test2@etcdev.ru",
                "Second Test2");

        ItemDto itemDto = new ItemDto(
                1L,
                "Перфоратор",
                "Электрический",
                true,
                1, null);
        BookingDto bookingDto = new BookingDto(1L,
                Booking.BookingStatus.WAITING,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(8),
                itemDto,
                bookerDto
        );

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }
}