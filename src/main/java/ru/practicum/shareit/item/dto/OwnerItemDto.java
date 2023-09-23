package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.ArrayList;
import java.util.List;

@Validated
@AllArgsConstructor
@Setter
@Getter
public class OwnerItemDto extends ItemDto {
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private final List<CommentDto> comments = new ArrayList<>();

    public OwnerItemDto(ItemDto itemDto) {
        super(itemDto.getId(), itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable(), itemDto.getOwnerId());
    }

    public OwnerItemDto(ItemDto itemDto, Booking lastBooking, Booking nextBooking) {
        super(itemDto.getId(), itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable(), itemDto.getOwnerId());
        if (lastBooking != null)
            this.lastBooking = BookingMapper.toBookingDto(lastBooking);
        if (nextBooking != null)
            this.nextBooking = BookingMapper.toBookingDto(nextBooking);

    }
}
