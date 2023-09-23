package ru.practicum.shareit.booking;

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
                .itemId(booking.getItem().getId())
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .bookerId(booking.getBooker().getId())
                .build();
    }

    public static Booking toBookingEntity(BookingDto bookingDto, Item item, User booker) {
        return Booking.builder()
                .id(bookingDto.getId())
                .status(bookingDto.getStatus())
                .startDate(bookingDto.getStart())
                .endDate(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .build();
    }
}
