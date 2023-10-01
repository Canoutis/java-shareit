package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.OwnerItemDto;

import java.util.Collection;
import java.util.List;

public interface ItemService {
    ItemDto create(Integer userId, ItemDto itemDto);

    ItemDto update(Integer userId, Long itemId, ItemDto itemDto);

    Collection<OwnerItemDto> getItemsByOwnerId(int userId);

    List<ItemDto> searchItems(String text);

    OwnerItemDto getItemById(long itemId, Integer userId);

    CommentDto saveComment(CommentDto commentDto, int userId, long itemId);
}
