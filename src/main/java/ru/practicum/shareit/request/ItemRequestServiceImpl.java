package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    ItemRequestRepository itemRequestRepository;
    ItemRepository itemRepository;
    UserRepository userRepository;

    @Autowired
    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository, UserRepository userRepository,
                                  ItemRepository itemRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional
    public ItemRequestDto create(Integer userId, ItemRequestDto itemRequestDto) {
        User owner = findUser(userId);
        return ItemRequestMapper.toItemRequestDto(
                itemRequestRepository.save(ItemRequestMapper.toItemRequestEntity(itemRequestDto, owner)));
    }

    @Override
    public Collection<ItemRequestDto> findUserItemRequests(Integer userId) {
        findUser(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findByOwnerIdIs(userId, Sort.by(Sort.Direction.DESC, "created"));
        return fillItemRequestsWithItems(itemRequests);


    }

    @Override
    public Collection<ItemRequestDto> findOtherItemRequests(int userId, Integer from, Integer size) {
        findUser(userId);
        if (from < 0 || size <= 0) {
            throw new BadRequestException(String.format("Неправильные значения параметров! from=%x size=%x", from, size));
        }
        PageRequest pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> itemRequests = itemRequestRepository.findDistinctByOwnerIdNot(userId, pageable);
        return fillItemRequestsWithItems(itemRequests);
    }

    @Override
    public ItemRequestDto findById(int userId, Long itemRequestId) {
        findUser(userId);
        Optional<ItemRequest> itemRequest = itemRequestRepository.findById(itemRequestId);
        if (itemRequest.isEmpty()) {
            throw new ObjectNotFoundException(String.format("Запрос вещи не найден! Id=%d", itemRequestId));
        } else {
            ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest.get());
            itemRequestDto.getItems().addAll(itemRepository.findByItemRequestIdOrderByIdAsc(itemRequestId)
                    .stream().map(ItemMapper::toItemDto).collect(Collectors.toList()));
            return itemRequestDto;
        }
    }

    private User findUser(int userId) {
        Optional<User> owner = userRepository.findById(userId);
        if (owner.isEmpty()) {
            throw new ObjectNotFoundException(String.format("Пользователь не найден! Id=%d", userId));
        }
        return owner.get();
    }

    private Collection<ItemRequestDto> fillItemRequestsWithItems(List<ItemRequest> itemRequests) {
        Map<Long, ItemRequestDto> itemRequestMap = itemRequests.stream()
                .collect(Collectors.toMap(ItemRequest::getId, ItemRequestMapper::toItemRequestDto));
        List<Item> items = itemRepository.findItemsByRequest(itemRequestMap.keySet(), Sort.by("id"));
        for (Item item : items) {
            itemRequestMap.get(item.getItemRequest().getId()).getItems().add(ItemMapper.toItemDto(item));
        }
        return itemRequestMap.values();
    }
}
