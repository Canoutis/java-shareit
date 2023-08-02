package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item addItem(Item item);

    Item updateItem(Item item);

    Item getItemById(int itemId);

    List<Item> getItemsByOwnerId(int userId);

    List<Item> searchItems(String text);
}
