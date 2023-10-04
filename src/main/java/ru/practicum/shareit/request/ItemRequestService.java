package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;


public interface ItemRequestService {
    ItemRequestDto create(Integer userId, ItemRequestDto itemRequestDto);

    Collection<ItemRequestDto> findUserItemRequests(Integer userId);

    Collection<ItemRequestDto> findOtherItemRequests(int userId, Integer from, Integer size);

    ItemRequestDto findById(int userId, Long itemRequestId);
}
