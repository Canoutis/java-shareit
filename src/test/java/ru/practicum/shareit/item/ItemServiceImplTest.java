package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.ObjectAccessException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ObjectSaveException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.OwnerItemDto;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static ru.practicum.shareit.booking.Booking.BookingStatus.APPROVED;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void testCreateItemOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        itemService.create(1, ItemMapper.toItemDto(item));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(itemRepository, Mockito.times(1))
                .save(any(Item.class));

        Mockito.verifyNoMoreInteractions(userRepository, itemRepository);

    }

    @Test
    void testCreateItemThrowsException() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(
                ObjectNotFoundException.class,
                () -> itemService.create(1, ItemMapper.toItemDto(item)));

        Assertions.assertEquals("Пользователь не найден! Id=1", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(itemRepository, Mockito.times(0))
                .save(any(Item.class));

        Mockito.verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void testCreateItemThrowsItemRequestObjectNotFoundException() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужен мощный лобзик!")
                .created(LocalDateTime.now())
                .build();
        Item item = new Item(1L, "Лобзик", "мощный", true, user,
                ItemRequestMapper.toItemRequestEntity(itemRequestDto, user2));
        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findById(1L))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(
                ObjectNotFoundException.class,
                () -> itemService.create(1, ItemMapper.toItemDto(item)));

        Assertions.assertEquals("Запрос не найден! Id=1", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findById(1L);

        Mockito.verifyNoMoreInteractions(userRepository, itemRequestRepository);
    }

    @Test
    void testUpdateItemOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Item toUpdateItem = new Item(1L, "Перфоратор-дрель", "Электрический, беспроводной", true, user, null);
        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findOwnerLastBooking(anyLong(), any(Sort.class)))
                .thenReturn(new ArrayList<>());
        Mockito.when(bookingRepository.findOwnerNextBooking(anyLong(), any(Sort.class)))
                .thenReturn(new ArrayList<>());
        Mockito.when(commentRepository.findByItem_IdIsOrderByCreatedDesc(1L))
                .thenReturn(new ArrayList<>());
        Mockito.when(itemRepository.save(any(Item.class)))
                .thenReturn(toUpdateItem);

        itemService.update(1, 1L, ItemMapper.toItemDto(toUpdateItem));

        Mockito.verify(userRepository, Mockito.times(2))
                .findById(1);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findOwnerLastBooking(anyLong(), any(Sort.class));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findOwnerNextBooking(anyLong(), any(Sort.class));
        Mockito.verify(commentRepository, Mockito.times(1))
                .findByItem_IdIsOrderByCreatedDesc(1L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .save(any(Item.class));

        Mockito.verifyNoInteractions(itemRequestRepository);

    }

    @Test
    void testUpdateItemThrowsAccessException() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);

        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        final ObjectAccessException exception = Assertions.assertThrows(
                ObjectAccessException.class,
                () -> itemService.update(2, 1L, ItemMapper.toItemDto(item)));

        Assertions.assertEquals("Ошибка доступа к вещи. Доступ к изменению информации о вещи запрещен! Id пользователя=2", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(2))
                .findById(2);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void testUpdateItemThrowsObjectNotFoundException() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);

        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.empty());

        final ObjectNotFoundException exception = Assertions.assertThrows(
                ObjectNotFoundException.class,
                () -> itemService.update(2, 1L, ItemMapper.toItemDto(item)));

        Assertions.assertEquals("Пользователь не найден! Id=2", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testGetItemsByOwnerIdOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item1 = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Item item2 = new Item(2L, "Перфоратор2", "Электрический2", true, user, null);

        Booking booking1 = new Booking(1L, APPROVED,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(12), item1, user2);
        Booking booking2 = new Booking(1L, APPROVED,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(2), item1, user2);

        Booking booking3 = new Booking(1L, APPROVED,
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), item1, user2);
        Booking booking4 = new Booking(1L, APPROVED,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), item1, user2);

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findByOwnerIdIs(anyInt(), any(PageRequest.class)))
                .thenReturn(Arrays.asList(item1, item2));
        Mockito.when(bookingRepository.findApprovedBookings(anyCollection(), any(Sort.class)))
                .thenReturn(Arrays.asList(booking1, booking2, booking3, booking4));
        Mockito.when(commentRepository.findCommentsByItems(anyCollection(), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        Collection<OwnerItemDto> items = itemService.getItemsByOwnerId(1, 0, 2);
        Assertions.assertNotNull(items);
        Assertions.assertEquals(2, items.size());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findByOwnerIdIs(anyInt(), any(PageRequest.class));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findApprovedBookings(anyCollection(), any(Sort.class));
        Mockito.verify(commentRepository, Mockito.times(1))
                .findCommentsByItems(anyCollection(), any(Sort.class));

        Mockito.verifyNoInteractions(itemRequestRepository);
    }

    @Test
    void testGetItemByIdByOwnerUserOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        User user3 = new User(3, "test3@etcdev.ru", "Test3 Test3");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking1 = new Booking(1L, Booking.BookingStatus.APPROVED,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3), item, user2);
        Booking booking2 = new Booking(2L, Booking.BookingStatus.APPROVED,
                LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(5), item, user3);
        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findOwnerLastBooking(anyLong(), any(Sort.class)))
                .thenReturn(Collections.singletonList(booking1));
        Mockito.when(bookingRepository.findOwnerNextBooking(anyLong(), any(Sort.class)))
                .thenReturn(Collections.singletonList(booking2));
        Mockito.when(commentRepository.findByItem_IdIsOrderByCreatedDesc(1L))
                .thenReturn(new ArrayList<>());

        OwnerItemDto receivedItem = itemService.getItemById(1, 1);
        Assertions.assertNotNull(receivedItem);
        Assertions.assertNotNull(receivedItem.getLastBooking());
        Assertions.assertNotNull(receivedItem.getNextBooking());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findOwnerLastBooking(anyLong(), any(Sort.class));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findOwnerNextBooking(anyLong(), any(Sort.class));
        Mockito.verify(commentRepository, Mockito.times(1))
                .findByItem_IdIsOrderByCreatedDesc(1L);

        Mockito.verifyNoInteractions(itemRequestRepository);

    }

    @Test
    void testGetItemByIdByNonOwnerUserOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Mockito.when(userRepository.findById(3))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(commentRepository.findByItem_IdIsOrderByCreatedDesc(1L))
                .thenReturn(new ArrayList<>());

        OwnerItemDto receivedItem = itemService.getItemById(1, 3);
        Assertions.assertNotNull(receivedItem);
        Assertions.assertNull(receivedItem.getLastBooking());
        Assertions.assertNull(receivedItem.getNextBooking());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(3);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(commentRepository, Mockito.times(1))
                .findByItem_IdIsOrderByCreatedDesc(1L);

        Mockito.verifyNoInteractions(bookingRepository, itemRequestRepository);

    }

    @Test
    void testSaveCommentOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Перфоратор в хорошем состоянии!")
                .authorName(user2.getName())
                .created(LocalDateTime.now())
                .build();
        Booking booking = new Booking(1L, Booking.BookingStatus.APPROVED,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3), item, user2);
        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findExpiredByBookerIdAndItemId(2, 1))
                .thenReturn(Collections.singletonList(booking));
        Mockito.when(commentRepository.save(any(Comment.class)))
                .thenReturn(CommentMapper.toCommentEntity(commentDto, user2, item));

        itemService.saveComment(commentDto, 2, 1);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findExpiredByBookerIdAndItemId(2, 1);
        Mockito.verify(commentRepository, Mockito.times(1))
                .save(any(Comment.class));

        Mockito.verifyNoInteractions(itemRequestRepository);
    }

    @Test
    void testSaveCommentThrowsObjectSaveException() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Перфоратор в хорошем состоянии!")
                .authorName(user2.getName())
                .created(LocalDateTime.now())
                .build();
        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findExpiredByBookerIdAndItemId(2, 1))
                .thenReturn(new ArrayList<>());

        final ObjectSaveException exception = Assertions.assertThrows(
                ObjectSaveException.class,
                () -> itemService.saveComment(commentDto, 2, 1));
        Assertions.assertEquals("Вы не можете оставить отзыв этому товару!", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findExpiredByBookerIdAndItemId(2, 1);

        Mockito.verifyNoInteractions(itemRequestRepository, commentRepository);
    }

    @Test
    void testSearchItemsOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Mockito.when(itemRepository.findItemsByText(anyString(), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(item));
        itemService.searchItems("Перфоратор", 0, 2);

        Mockito.verify(itemRepository, Mockito.times(1))
                .findItemsByText(anyString(), any(PageRequest.class));

        Mockito.verifyNoInteractions(itemRequestRepository, commentRepository, userRepository, bookingRepository);
    }

}
