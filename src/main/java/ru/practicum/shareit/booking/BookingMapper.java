package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .status(booking.getStatus())
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .item(ItemMapper.toItemDto(booking.getItem()))
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .build();
    }
    public static BookItemRequestDto toBookingRequestDto(Booking booking) {
        return new BookItemRequestDto(booking.getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getItem().getId(),
                booking.getBooker().getId());

    }

    public static Booking toBookingEntity(BookItemRequestDto bookItemRequestDto, Item item, User booker) {
        return Booking.builder()
                .startDate(bookItemRequestDto.getStart())
                .endDate(bookItemRequestDto.getEnd())
                .item(item)
                .booker(booker)
                .build();
    }
}
