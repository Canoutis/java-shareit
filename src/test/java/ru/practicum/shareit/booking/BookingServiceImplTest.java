package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectSaveException;
import ru.practicum.shareit.exception.ObjectUpdateException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static ru.practicum.shareit.booking.Booking.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.Booking.BookingStatus.REJECTED;
import static ru.practicum.shareit.booking.Booking.BookingStatus.WAITING;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void testCreateBookingOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        BookItemRequestDto bookItemRequestDto = new BookItemRequestDto();
        bookItemRequestDto.setItemId(item.getId());
        bookItemRequestDto.setBookerId(user2.getId());
        bookItemRequestDto.setStart(LocalDateTime.now().plusDays(5));
        bookItemRequestDto.setEnd(LocalDateTime.now().plusDays(10));

        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.hasApprovedBookingInPeriod(1L,
                        bookItemRequestDto.getStart(), bookItemRequestDto.getEnd()))
                .thenReturn(false);
        Mockito.when(bookingRepository.save(any(Booking.class)))
                .thenReturn(BookingMapper.toBookingEntity(bookItemRequestDto, item, user2));

        bookingService.create(bookItemRequestDto, user2.getId());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .hasApprovedBookingInPeriod(1L, bookItemRequestDto.getStart(), bookItemRequestDto.getEnd());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(any(Booking.class));

    }

    @Test
    void testCreateBookingThrowsObjectSaveException() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", false, user, null);
        BookItemRequestDto bookItemRequestDto = new BookItemRequestDto();
        bookItemRequestDto.setItemId(item.getId());
        bookItemRequestDto.setBookerId(user2.getId());
        bookItemRequestDto.setStart(LocalDateTime.now().plusDays(5));
        bookItemRequestDto.setEnd(LocalDateTime.now().plusDays(10));

        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));


        final ObjectSaveException exception = Assertions.assertThrows(
                ObjectSaveException.class,
                () -> bookingService.create(bookItemRequestDto, user2.getId()));

        Assertions.assertEquals("Вещь недоступна для бронирования! Id=1", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(bookingRepository);

    }

    @Test
    void testCreateBookingThrowsObjectUpdateException() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        BookItemRequestDto bookItemRequestDto = new BookItemRequestDto();
        bookItemRequestDto.setItemId(item.getId());
        bookItemRequestDto.setBookerId(user.getId());
        bookItemRequestDto.setStart(LocalDateTime.now().plusDays(5));
        bookItemRequestDto.setEnd(LocalDateTime.now().plusDays(10));

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));


        final ObjectUpdateException exception = Assertions.assertThrows(
                ObjectUpdateException.class,
                () -> bookingService.create(bookItemRequestDto, user.getId()));

        Assertions.assertEquals("Вещь недоступна для бронирования! Id=1", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(bookingRepository);

    }

    @Test
    void testCreateBookingThrowsObjectSaveExceptionBusy() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        BookItemRequestDto bookItemRequestDto = new BookItemRequestDto();
        bookItemRequestDto.setItemId(item.getId());
        bookItemRequestDto.setBookerId(user2.getId());
        bookItemRequestDto.setStart(LocalDateTime.now().plusDays(5));
        bookItemRequestDto.setEnd(LocalDateTime.now().plusDays(10));

        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.hasApprovedBookingInPeriod(1L,
                        bookItemRequestDto.getStart(), bookItemRequestDto.getEnd()))
                .thenReturn(true);

        final ObjectSaveException exception = Assertions.assertThrows(
                ObjectSaveException.class,
                () -> bookingService.create(bookItemRequestDto, user2.getId()));

        Assertions.assertEquals("Некорректный период бронирования!", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .hasApprovedBookingInPeriod(1L, bookItemRequestDto.getStart(), bookItemRequestDto.getEnd());
        Mockito.verify(bookingRepository, Mockito.times(0))
                .save(any(Booking.class));
    }


    @Test
    void testApproveBookingOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, WAITING,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10), item, user2);

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDto bookingDto = bookingService.approve(1L, 1, true);
        Assertions.assertEquals(APPROVED, bookingDto.getStatus());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(any(Booking.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testRejectBookingOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, WAITING,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10), item, user2);

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDto bookingDto = bookingService.approve(1L, 1, false);
        Assertions.assertEquals(REJECTED, bookingDto.getStatus());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(any(Booking.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testRejectBookingThrowsBadRequestException() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, REJECTED,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10), item, user2);

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));


        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> bookingService.approve(1L, 1, true));
        Assertions.assertEquals("Изменение статуса бронирования недоступно! Id=1", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testFindBookingsBySearchStateALLOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, WAITING,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(10), item, user2);

        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findByBookerId(anyInt(), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookingDtoList = bookingService.findBookingsBySearchState(2, State.ALL, 0, 10);

        Assertions.assertEquals(1, bookingDtoList.size());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByBookerId(anyInt(), any(PageRequest.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testFindBookingsBySearchStateCURRENTOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, WAITING,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(10), item, user2);

        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findCurrentBookingsByBookerId(anyInt(), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookingDtoList = bookingService.findBookingsBySearchState(2, State.CURRENT, 0, 10);

        Assertions.assertEquals(1, bookingDtoList.size());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findCurrentBookingsByBookerId(anyInt(), any(PageRequest.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testFindBookingsBySearchStatePASTOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, APPROVED,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(2), item, user2);

        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findPastBookingsByBookerId(anyInt(), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookingDtoList = bookingService.findBookingsBySearchState(2, State.PAST, 0, 10);

        Assertions.assertEquals(1, bookingDtoList.size());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findPastBookingsByBookerId(anyInt(), any(PageRequest.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testFindBookingsBySearchStateFUTUREOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, APPROVED,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(12), item, user2);

        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findFutureBookingsByBookerId(anyInt(), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookingDtoList = bookingService.findBookingsBySearchState(2, State.FUTURE, 0, 10);

        Assertions.assertEquals(1, bookingDtoList.size());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findFutureBookingsByBookerId(anyInt(), any(PageRequest.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testFindBookingsBySearchStateWAITINGOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, WAITING,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(12), item, user2);

        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findBookingsByBookerIdAndBookingStatus(anyInt(), any(Booking.BookingStatus.class), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookingDtoList = bookingService.findBookingsBySearchState(2, State.WAITING, 0, 10);

        Assertions.assertEquals(1, bookingDtoList.size());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findBookingsByBookerIdAndBookingStatus(anyInt(), any(Booking.BookingStatus.class), any(PageRequest.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testFindBookingsBySearchStateREJECTEDOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, REJECTED,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(12), item, user2);

        Mockito.when(userRepository.findById(2))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findBookingsByBookerIdAndBookingStatus(anyInt(), any(Booking.BookingStatus.class), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookingDtoList = bookingService.findBookingsBySearchState(2, State.REJECTED, 0, 10);

        Assertions.assertEquals(1, bookingDtoList.size());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findBookingsByBookerIdAndBookingStatus(anyInt(), any(Booking.BookingStatus.class), any(PageRequest.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testFindBookingsByItemsOwnerALLOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, WAITING,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(10), item, user2);

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findByOwnerId(anyInt(), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookingDtoList = bookingService.findBookingsByItemsOwner(1, State.ALL, 0, 10);

        Assertions.assertEquals(1, bookingDtoList.size());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByOwnerId(anyInt(), any(PageRequest.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testFindBookingsByItemsOwnerCURRENTOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, WAITING,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(10), item, user2);

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findCurrentBookingsByOwnerId(anyInt(), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookingDtoList = bookingService.findBookingsByItemsOwner(1, State.CURRENT, 0, 10);

        Assertions.assertEquals(1, bookingDtoList.size());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findCurrentBookingsByOwnerId(anyInt(), any(PageRequest.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testFindBookingsByItemsOwnerPASTOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, APPROVED,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(2), item, user2);

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findPastBookingsByOwnerId(anyInt(), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookingDtoList = bookingService.findBookingsByItemsOwner(1, State.PAST, 0, 10);

        Assertions.assertEquals(1, bookingDtoList.size());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findPastBookingsByOwnerId(anyInt(), any(PageRequest.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testFindBookingsByItemsOwnerFUTUREOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, APPROVED,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(12), item, user2);

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findFutureBookingsByOwnerId(anyInt(), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookingDtoList = bookingService.findBookingsByItemsOwner(1, State.FUTURE, 0, 10);

        Assertions.assertEquals(1, bookingDtoList.size());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findFutureBookingsByOwnerId(anyInt(), any(PageRequest.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testFindBookingsByItemsOwnerWAITINGOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, WAITING,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(12), item, user2);

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findBookingsByOwnerIdAndBookingStatus(anyInt(), any(Booking.BookingStatus.class), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookingDtoList = bookingService.findBookingsByItemsOwner(1, State.WAITING, 0, 10);

        Assertions.assertEquals(1, bookingDtoList.size());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findBookingsByOwnerIdAndBookingStatus(anyInt(), any(Booking.BookingStatus.class), any(PageRequest.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

    @Test
    void testFindBookingsByItemsOwnerREJECTEDOk() {
        User user = new User(1, "test@etcdev.ru", "Test Test");
        User user2 = new User(2, "test2@etcdev.ru", "Test2 Test2");
        Item item = new Item(1L, "Перфоратор", "Электрический", true, user, null);
        Booking booking = new Booking(1L, REJECTED,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(12), item, user2);

        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findBookingsByOwnerIdAndBookingStatus(anyInt(), any(Booking.BookingStatus.class), any(PageRequest.class)))
                .thenReturn(Collections.singletonList(booking));
        List<BookingDto> bookingDtoList = bookingService.findBookingsByItemsOwner(1, State.REJECTED, 0, 10);

        Assertions.assertEquals(1, bookingDtoList.size());

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findBookingsByOwnerIdAndBookingStatus(anyInt(), any(Booking.BookingStatus.class), any(PageRequest.class));
        Mockito.verifyNoInteractions(itemRepository);

    }

}
