package ru.practicum.shareit.item;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;

import java.util.List;

@Service
public class ItemService {

    private final ItemStorage itemStorage;

    public ItemService(ItemStorage itemStorage) {
        this.itemStorage = itemStorage;
    }

    public ResponseEntity<Item> addItem(int userId, Item item) {
        Item addedItem = itemStorage.addItem(userId, item);
        return new ResponseEntity<>(addedItem, HttpStatus.CREATED);
    }

    public ResponseEntity<Item> updateItem(int userId, int itemId, ItemDto itemDto) {
        Item updatedItem = itemStorage.updateItem(userId, itemId, itemDto);
        if (updatedItem != null) {
            return new ResponseEntity<>(updatedItem, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Item> getItemById(int itemId) {
        Item item = itemStorage.getItemById(itemId);
        if (item != null) {
            return new ResponseEntity<>(item, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<List<Item>> getItemsByOwnerId(int userId) {
        List<Item> items = itemStorage.getItemsByOwnerId(userId);
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    public ResponseEntity<List<Item>> searchItems(String text) {
        List<Item> foundItems = itemStorage.searchItems(text);
        return new ResponseEntity<>(foundItems, HttpStatus.OK);
    }
}
