package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Validated
@AllArgsConstructor
@Builder
public class BookingDto {
    private Long id;
    private Booking.BookingStatus status;
    @NotNull
    private LocalDateTime start;
    @NotNull
    private LocalDateTime end;
    private ItemDto item;
    private long itemId;
    private UserDto booker;
    private int bookerId;
}
