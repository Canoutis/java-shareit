package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class ItemDto {
    private int id;
    private String name;
    private String description;
    private Boolean available;
}
