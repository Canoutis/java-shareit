package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingDto bookingDto, int userId);

    BookingDto approve(Long bookingId, int userId, boolean approve);

    BookingDto findById(Long bookingId, Integer userId);

    List<BookingDto> findBookingsBySearchState(int userId, String state);

    List<BookingDto> findBookingsByItemsOwner(int userId, String state);
}
