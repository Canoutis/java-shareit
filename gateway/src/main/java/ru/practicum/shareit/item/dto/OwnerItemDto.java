package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.util.ArrayList;
import java.util.List;

@Validated
@AllArgsConstructor
@Setter
@Getter
public class OwnerItemDto extends ItemDto {
    private BookItemRequestDto lastBooking;
    private BookItemRequestDto nextBooking;
    private final List<CommentDto> comments = new ArrayList<>();

}
