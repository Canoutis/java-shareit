package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(BookItemRequestDto bookItemRequestDto, int userId);

    BookingDto approve(Long bookingId, int userId, boolean approve);

    BookingDto findById(Long bookingId, Integer userId);

    List<BookingDto> findBookingsBySearchState(int userId, BookingState bookingState, Integer from, Integer size);

    List<BookingDto> findBookingsByItemsOwner(int userId, BookingState bookingState, Integer from, Integer size);
}
