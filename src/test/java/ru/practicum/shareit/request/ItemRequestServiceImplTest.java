package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith({MockitoExtension.class})
public class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestServiceImpl;

    @Test
    void testCreateOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужен мощный лобзик!")
                .created(LocalDateTime.now())
                .build();

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.save(any(ItemRequest.class)))
                .thenReturn(ItemRequestMapper.toItemRequestEntity(itemRequestDto, user));

        itemRequestServiceImpl.create(1, itemRequestDto);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .save(any(ItemRequest.class));
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void testFindByIdOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужен мощный лобзик!")
                .created(LocalDateTime.now())
                .build();

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findById(1L))
                .thenReturn(Optional.of(ItemRequestMapper.toItemRequestEntity(itemRequestDto, user)));
        Mockito.when(itemRepository.findByItemRequestId(anyLong(), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        itemRequestServiceImpl.findById(1, 1L);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findByItemRequestId(anyLong(), any(Sort.class));
    }

    @Test
    void testFindByIdThrowsObjectNotFoundException() {
        User user = new User(1, "test@etcdev.ru", "Test Test");

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findById(1L))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(
                ObjectNotFoundException.class,
                () -> itemRequestServiceImpl.findById(1, 1L));

        Assertions.assertEquals("Запрос вещи не найден! Id=1", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void testFindByIdThrowsUserObjectNotFoundException() {
        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(
                ObjectNotFoundException.class,
                () -> itemRequestServiceImpl.findById(1, 1L));

        Assertions.assertEquals("Пользователь не найден! Id=1", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verifyNoInteractions(itemRepository, itemRequestRepository);
    }

    @Test
    void testFindUserItemRequestsOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужен мощный лобзик!")
                .created(LocalDateTime.now())
                .build();

        Item item = new Item(1L, "Лобзик", "Мощный", true, user,
                ItemRequestMapper.toItemRequestEntity(itemRequestDto, user2));
        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findByOwnerIdIs(anyInt(), any(Sort.class)))
                .thenReturn(Collections.singletonList(ItemRequestMapper.toItemRequestEntity(itemRequestDto, user)));
        Mockito.when(itemRepository.findItemsByRequest(anyCollection(), any(Sort.class)))
                .thenReturn(Collections.singletonList(item));

        itemRequestServiceImpl.findUserItemRequests(1);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findByOwnerIdIs(anyInt(), any(Sort.class));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findItemsByRequest(anyCollection(), any(Sort.class));
    }

    @Test
    void testFindOtherItemRequestsOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужен мощный лобзик!")
                .created(LocalDateTime.now())
                .build();

        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findDistinctByOwnerIdNot(anyInt(), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(ItemRequestMapper.toItemRequestEntity(itemRequestDto, user)));
        Mockito.when(itemRepository.findItemsByRequest(anyCollection(), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        itemRequestServiceImpl.findOtherItemRequests(2, 0, 2);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2);
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findDistinctByOwnerIdNot(anyInt(), any(PageRequest.class));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findItemsByRequest(anyCollection(), any(Sort.class));
    }
}
