package ru.practicum.shareit.item.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ItemInMemoryStorage implements ItemStorage {
    UserStorage userStorage;

    @Autowired
    public ItemInMemoryStorage(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private final Map<Integer, List<Item>> userItemsMap = new HashMap<>();
    private final Map<Integer, Item> allItemsMap = new HashMap<>();
    private int itemIdCounter = 1;

    @Override
    public Item addItem(int userId, Item item) {
        userStorage.getUserById(userId);
        item.setAvailable(true);
        item.setId(itemIdCounter++);
        item.setOwnerId(userId);
        allItemsMap.put(item.getId(), item);

        List<Item> userItems = userItemsMap.getOrDefault(userId, new ArrayList<>());
        userItems.add(item);
        userItemsMap.put(userId, userItems);

        return item;
    }

    @Override
    public Item updateItem(int userId, int itemId, ItemDto itemDto) {
        if (!allItemsMap.containsKey(itemId) || userId != allItemsMap.get(itemId).getOwnerId()) {
            throw new ObjectNotFoundException(
                    String.format("Ошибка обновления вещи. Вещь или пользователь не найдены! Id вещи=%d", itemId));
        }
        Item tempItem = allItemsMap.get(itemId);

        if (itemDto.getAvailable() != null)
            tempItem.setAvailable(itemDto.getAvailable());

        if (itemDto.getName() != null && !itemDto.getName().isBlank())
            tempItem.setName(itemDto.getName());

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank())
            tempItem.setDescription(itemDto.getDescription());

        allItemsMap.put(tempItem.getId(), tempItem);
        return tempItem;
    }

    @Override
    public Item getItemById(int itemId) {
        return allItemsMap.get(itemId);
    }

    @Override
    public List<Item> getItemsByOwnerId(int userId) {
        return userItemsMap.getOrDefault(userId, new ArrayList<>());
    }

    @Override
    public List<Item> searchItems(String text) {
        List<Item> foundItems = new ArrayList<>();
        for (Item item : allItemsMap.values()) {
            if (item.getAvailable() && !text.isBlank() &&
                    (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                            item.getDescription().toLowerCase().contains(text.toLowerCase()))) {
                foundItems.add(item);
            }
        }
        return foundItems;
    }
}
