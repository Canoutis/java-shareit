package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSerializeBookingDto() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(2);

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .status(Booking.BookingStatus.APPROVED)
                .start(start)
                .end(end)
                .build();

        String json = objectMapper.writeValueAsString(bookingDto);

        assertThat(json).isEqualTo("{\"id\":1,\"status\":\"APPROVED\",\"start\":\"" +
                start.toString() + "\",\"end\":\"" + end.toString() + "\",\"item\":null,\"booker\":null}");
    }

    @Test
    public void testDeserializeBookingDto() throws Exception {
        String json = "{\"id\":1,\"status\":\"APPROVED\",\"start\":\"2023-10-04T15:30:00\"," +
                "\"end\":\"2023-10-04T17:30:00\",\"item\":null,\"booker\":null}";

        BookingDto bookingDto = objectMapper.readValue(json, BookingDto.class);

        assertThat(bookingDto.getId()).isEqualTo(1L);
        assertThat(bookingDto.getStatus()).isEqualTo(Booking.BookingStatus.APPROVED);
        assertThat(bookingDto.getStart()).isEqualTo(LocalDateTime.parse("2023-10-04T15:30:00"));
        assertThat(bookingDto.getEnd()).isEqualTo(LocalDateTime.parse("2023-10-04T17:30:00"));
        assertThat(bookingDto.getItem()).isNull();
        assertThat(bookingDto.getBooker()).isNull();
    }
}