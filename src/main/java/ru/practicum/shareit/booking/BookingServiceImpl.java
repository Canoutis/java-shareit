package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ObjectSaveException;
import ru.practicum.shareit.exception.ObjectUpdateException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.Booking.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.Booking.BookingStatus.REJECTED;
import static ru.practicum.shareit.booking.Booking.BookingStatus.WAITING;

@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    public final Sort BOOKING_START_DATE_SORT_DESC = Sort.by(Sort.Direction.DESC, "startDate");

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                              ItemRepository itemRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BookingDto create(BookItemRequestDto bookItemRequestDto, int userId) {
        Optional<User> booker = userRepository.findById(userId);
        if (booker.isEmpty()) {
            throw new ObjectNotFoundException(String.format("Пользователь не найден! Id=%d", userId));
        } else {
            Optional<Item> item = itemRepository.findById(bookItemRequestDto.getItemId());
            if (item.isPresent()) {
                if (!item.get().getAvailable()) {
                    throw new ObjectSaveException(String.format("Вещь недоступна для бронирования! Id=%d", item.get().getId()));
                } else if (item.get().getOwner().getId() == userId) {
                    throw new ObjectUpdateException(String.format("Вещь недоступна для бронирования! Id=%d", item.get().getId()));
                } else if (!bookItemRequestDto.getStart().isBefore(bookItemRequestDto.getEnd()) ||
                        bookItemRequestDto.getStart().isBefore(LocalDateTime.now()) ||
                        bookingRepository.hasApprovedBookingInPeriod(bookItemRequestDto.getItemId(),
                                bookItemRequestDto.getStart(), bookItemRequestDto.getEnd())) {
                    throw new ObjectSaveException("Некорректный период бронирования!");
                }
                Booking booking = BookingMapper.toBookingEntity(bookItemRequestDto, item.get(), booker.get());
                booking.setStatus(WAITING);
                return BookingMapper.toBookingDto(bookingRepository.save(booking));
            } else {
                throw new ObjectNotFoundException(String.format("Вещь не найдена! Id=%d", bookItemRequestDto.getItemId()));
            }

        }
    }

    @Override
    @Transactional
    public BookingDto approve(Long bookingId, int userId, boolean approve) {
        Booking booking = getById(bookingId, userId);
        if (booking.getItem().getOwner().getId() != userId)
            throw new ObjectNotFoundException(String.format("Бронирование не найдено! Id=%d", bookingId));
        if (booking.getStatus() != WAITING)
            throw new BadRequestException(String.format("Изменение статуса бронирования недоступно! Id=%d", bookingId));
        booking.setStatus(approve ? APPROVED : REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto findById(Long bookingId, Integer userId) {
        Booking booking = getById(bookingId, userId);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> findBookingsBySearchState(int userId, State state) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) throw new ObjectNotFoundException(String.format("Пользователь не найден! Id=%x", userId));
        List<Booking> bookings;
        if (state == null || state == State.ALL) {
            bookings = bookingRepository.findByBookerId(userId, BOOKING_START_DATE_SORT_DESC);
        } else if (state == State.CURRENT) {
            bookings = bookingRepository.findCurrentBookingsByBookerId(userId, BOOKING_START_DATE_SORT_DESC);
        } else if (state == State.PAST) {
            bookings = bookingRepository.findPastBookingsByBookerId(userId, BOOKING_START_DATE_SORT_DESC);
        } else if (state == State.FUTURE) {
            bookings = bookingRepository.findFutureBookingsByBookerId(userId, BOOKING_START_DATE_SORT_DESC);
        } else if (state == State.WAITING) {
            bookings = bookingRepository.findBookingsByBookerIdAndBookingStatus(userId, WAITING, BOOKING_START_DATE_SORT_DESC);
        } else if (state == State.REJECTED) {
            bookings = bookingRepository.findBookingsByBookerIdAndBookingStatus(userId, REJECTED, BOOKING_START_DATE_SORT_DESC);
        } else {
            throw new BadRequestException(String.format("Unknown state: %s", state));
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> findBookingsByItemsOwner(int userId, State state) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) throw new ObjectNotFoundException(String.format("Пользователь не найден! Id=%x", userId));
        List<Booking> bookings;
        if (state == null || state == State.ALL) {
            bookings = bookingRepository.findByOwnerId(userId, BOOKING_START_DATE_SORT_DESC);
        } else if (state == State.CURRENT) {
            bookings = bookingRepository.findCurrentBookingsByOwnerId(userId, BOOKING_START_DATE_SORT_DESC);
        } else if (state == State.PAST) {
            bookings = bookingRepository.findPastBookingsByOwnerId(userId, BOOKING_START_DATE_SORT_DESC);
        } else if (state == State.FUTURE) {
            bookings = bookingRepository.findFutureBookingsByOwnerId(userId, BOOKING_START_DATE_SORT_DESC);
        } else if (state == State.WAITING) {
            bookings = bookingRepository.findBookingsByOwnerIdAndBookingStatus(userId, WAITING, BOOKING_START_DATE_SORT_DESC);
        } else if (state == State.REJECTED) {
            bookings = bookingRepository.findBookingsByOwnerIdAndBookingStatus(userId, REJECTED, BOOKING_START_DATE_SORT_DESC);
        } else {
            throw new BadRequestException(String.format("Unknown state: %s", state));
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private Booking getById(Long bookingId, int userId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty() || (booking.get().getBooker().getId() != userId &&
                booking.get().getItem().getOwner().getId() != userId)) {
            throw new ObjectNotFoundException(String.format("Бронирование не найдено! Id=%d", bookingId));
        } else {
            return booking.get();
        }
    }
}
