package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookItemRequestDto {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private long itemId;
    private int bookerId;

}
