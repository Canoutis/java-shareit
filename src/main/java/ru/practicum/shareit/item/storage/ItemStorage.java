package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item addItem(int userId, Item item);

    Item updateItem(int userId, int itemId, ItemDto itemDto);

    Item getItemById(int itemId);

    List<Item> getItemsByOwnerId(int userId);

    List<Item> searchItems(String text);
}
