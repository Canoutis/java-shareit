package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                              ItemRepository itemRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public BookingDto create(BookingDto bookingDto, int userId) {
        Optional<User> booker = userRepository.findById(userId);
        if (booker.isEmpty()) {
            throw new ObjectNotFoundException(String.format("Пользователь не найден! Id=%d", userId));
        } else {
            Optional<Item> item = itemRepository.findById(bookingDto.getItemId());
            if (item.isPresent()) {
                if (!item.get().getAvailable()) {
                    throw new ObjectSaveException(String.format("Вещь недоступна для бронирования! Id=%d", item.get().getId()));
                } else if (item.get().getOwner().getId() == userId) {
                    throw new ObjectUpdateException(String.format("Вещь недоступна для бронирования! Id=%d", item.get().getId()));

                } else if (!bookingDto.getStart().isBefore(bookingDto.getEnd()) ||
                        bookingDto.getStart().isBefore(LocalDateTime.now())) {
                    throw new ObjectSaveException("Некорректный период бронирования!");
                }
                bookingDto.setStatus(WAITING);
                Booking booking = BookingMapper.toBookingEntity(bookingDto, item.get(), booker.get());
                return BookingMapper.toBookingDto(bookingRepository.save(booking));
            } else {
                throw new ObjectNotFoundException(String.format("Вещь не найдена! Id=%d", bookingDto.getItemId()));
            }

        }
    }

    @Override
    public BookingDto approve(Long bookingId, int userId, boolean approve) {
        Booking booking = getById(bookingId, userId);
        if (booking.getItem().getOwner().getId() != userId)
            throw new ObjectNotFoundException(String.format("Бронирование не найдено! Id=%d", bookingId));
        if (booking.getStatus() == APPROVED)
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
    public List<BookingDto> findBookingsBySearchState(int userId, String state) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) throw new ObjectNotFoundException(String.format("Пользователь не найден! Id=%x", userId));
        if (state == null || State.ALL.toString().equals(state)) {
            List<Booking> bookings = bookingRepository.findByBookerId(userId);
            return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        } else if (State.CURRENT.toString().equals(state)) {
            List<Booking> bookings = bookingRepository.findCurrentBookingsByBookerId(userId);
            return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        } else if (State.PAST.toString().equals(state)) {
            List<Booking> bookings = bookingRepository.findPastBookingsByBookerId(userId);
            return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        } else if (State.FUTURE.toString().equals(state)) {
            List<Booking> bookings = bookingRepository.findFutureBookingsByBookerId(userId);
            return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        } else if (State.WAITING.toString().equals(state)) {
            List<Booking> bookings = bookingRepository.findBookingsByBookerIdAndBookingStatus(userId, WAITING);
            return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        } else if (State.REJECTED.toString().equals(state)) {
            List<Booking> bookings = bookingRepository.findBookingsByBookerIdAndBookingStatus(userId, REJECTED);
            return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        } else {
            throw new BadRequestException(String.format("Unknown state: %s", state));
        }
    }

    @Override
    public List<BookingDto> findBookingsByItemsOwner(int userId, String state) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) throw new ObjectNotFoundException(String.format("Пользователь не найден! Id=%x", userId));
        if (state == null || State.ALL.toString().equals(state)) {
            List<Booking> bookings = bookingRepository.findByOwnerId(userId);
            return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        } else if (State.CURRENT.toString().equals(state)) {
            List<Booking> bookings = bookingRepository.findCurrentBookingsByOwnerId(userId);
            return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        } else if (State.PAST.toString().equals(state)) {
            List<Booking> bookings = bookingRepository.findPastBookingsByOwnerId(userId);
            return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        } else if (State.FUTURE.toString().equals(state)) {
            List<Booking> bookings = bookingRepository.findFutureBookingsByOwnerId(userId);
            return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        } else if (State.WAITING.toString().equals(state)) {
            List<Booking> bookings = bookingRepository.findBookingsByOwnerIdAndBookingStatus(userId, WAITING);
            return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        } else if (State.REJECTED.toString().equals(state)) {
            List<Booking> bookings = bookingRepository.findBookingsByOwnerIdAndBookingStatus(userId, REJECTED);
            return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        } else {
            throw new BadRequestException(String.format("Unknown state: %s", state));
        }
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
