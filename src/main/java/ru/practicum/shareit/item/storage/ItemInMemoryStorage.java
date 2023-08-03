package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ItemInMemoryStorage implements ItemStorage {

    private final Map<Integer, Map<Integer, Item>> userItemsMap = new HashMap<>();
    private final Map<Integer, Item> items = new HashMap<>();
    private int itemIdCounter = 1;

    @Override
    public Item addItem(Item item) {
        item.setId(itemIdCounter++);
        items.put(item.getId(), item);

        Map<Integer, Item> userItems = userItemsMap.getOrDefault(item.getOwner().getId(), new HashMap<>());
        userItems.put(item.getId(), item);
        userItemsMap.put(item.getOwner().getId(), userItems);

        return item;
    }

    @Override
    public Item updateItem(Item item) {
        Map<Integer, Item> userItems = userItemsMap.getOrDefault(item.getOwner().getId(), new HashMap<>());
        userItems.put(item.getId(), item);
        userItemsMap.put(item.getOwner().getId(), userItems);

        items.put(item.getId(), item);

        return item;
    }

    @Override
    public Item getItemById(int itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getItemsByOwnerId(int userId) {
        return new ArrayList<>(userItemsMap.getOrDefault(userId, new HashMap<>()).values());
    }

    @Override
    public List<Item> searchItems(String text) {
        List<Item> foundItems = new ArrayList<>();
        if (text != null && !text.isBlank()) {
            text = text.toLowerCase();
            for (Item item : items.values()) {
                if (item.getAvailable() &&
                        (item.getName().toLowerCase().contains(text) ||
                                item.getDescription().toLowerCase().contains(text))) {
                    foundItems.add(item);
                }
            }
        }
        return foundItems;
    }
}
