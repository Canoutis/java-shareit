package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectAccessException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public ItemService(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    public ItemDto addItem(int userId, ItemDto itemDto) {
        User owner = userStorage.getUserById(userId);
        if (owner == null) {
            throw new ObjectNotFoundException(String.format("Пользователь не найден! Id=%d", userId));
        }
        itemDto.setOwnerId(userId);
        return ItemMapper.toItemDto(itemStorage.addItem(ItemMapper.toItemEntity(itemDto, owner)));
    }

    public ItemDto updateItem(int userId, int itemId, ItemDto itemDto) {
        User user = userStorage.getUserById(userId);
        ItemDto tempItemDto = getItemById(itemId);
        if (tempItemDto.getOwnerId() != userId) {
            throw new ObjectAccessException(
                    String.format("Ошибка доступа к вещи. Доступ к изменению информации о вещи запрещен! Id пользователя=%d", userId));
        }

        if (itemDto.getAvailable() != null)
            tempItemDto.setAvailable(itemDto.getAvailable());

        if (itemDto.getName() != null && !itemDto.getName().isBlank())
            tempItemDto.setName(itemDto.getName());

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank())
            tempItemDto.setDescription(itemDto.getDescription());
        Item item = ItemMapper.toItemEntity(tempItemDto, user);
        return ItemMapper.toItemDto(itemStorage.updateItem(item));
    }

    public ItemDto getItemById(int itemId) {
        Item item = itemStorage.getItemById(itemId);
        if (item == null) {
            throw new ObjectNotFoundException(String.format("Вещь не найдена! Id=%d", itemId));
        }
        return ItemMapper.toItemDto(item);
    }

    public List<ItemDto> getItemsByOwnerId(int userId) {
        List<Item> items = itemStorage.getItemsByOwnerId(userId);
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    public List<ItemDto> searchItems(String text) {
        List<Item> items = itemStorage.searchItems(text);
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }
}
