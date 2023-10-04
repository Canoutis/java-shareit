package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
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
    void testFindUserItemRequestsOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужен мощный лобзик!")
                .created(LocalDateTime.now())
                .build();

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findByOwnerIdIs(anyInt(), any(Sort.class)))
                .thenReturn(Collections.singletonList(ItemRequestMapper.toItemRequestEntity(itemRequestDto, user)));
        Mockito.when(itemRepository.findItemsByRequest(anyCollection(), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        itemRequestServiceImpl.findUserItemRequests(1);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findByOwnerIdIs(anyInt(), any(Sort.class));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findItemsByRequest(anyCollection(), any(Sort.class));
    }

}
