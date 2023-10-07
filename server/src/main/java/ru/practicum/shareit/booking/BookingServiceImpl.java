package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.Booking.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.Booking.BookingStatus.REJECTED;
import static ru.practicum.shareit.booking.Booking.BookingStatus.WAITING;
import static ru.practicum.shareit.utils.Helper.findUserById;

@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private static final Sort bookingStartDateSortDesc = Sort.by(Sort.Direction.DESC, "startDate");

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
        User booker = findUserById(userRepository, userId);
        Optional<Item> item = itemRepository.findById(bookItemRequestDto.getItemId());
        if (item.isPresent()) {
            if (!item.get().getAvailable() || bookingRepository.hasApprovedBookingInPeriod(bookItemRequestDto.getItemId(),
                    bookItemRequestDto.getStart(), bookItemRequestDto.getEnd())) {
                throw new ObjectSaveException(String.format("Вещь недоступна для бронирования! Id=%d", item.get().getId()));
            } else if (item.get().getOwner().getId() == userId) {
                throw new ObjectUpdateException(String.format("Вещь недоступна для бронирования! Id=%d", item.get().getId()));
            }
            Booking booking = BookingMapper.toBookingEntity(bookItemRequestDto, item.get(), booker);
            booking.setStatus(WAITING);
            return BookingMapper.toBookingDto(bookingRepository.save(booking));
        } else {
            throw new ObjectNotFoundException(String.format("Вещь не найдена! Id=%d", bookItemRequestDto.getItemId()));
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
    public List<BookingDto> findBookingsBySearchState(int userId, BookingState bookingState, Integer from, Integer size) {
        findUserById(userRepository, userId);
        PageRequest pageable = PageRequest.of(from > 0 ? from / size : 0, size, bookingStartDateSortDesc);
        List<Booking> bookings;
        if (bookingState == null || bookingState == BookingState.ALL) {
            bookings = bookingRepository.findByBookerId(userId, pageable);
        } else if (bookingState == BookingState.CURRENT) {
            bookings = bookingRepository.findCurrentBookingsByBookerId(userId, pageable);
        } else if (bookingState == BookingState.PAST) {
            bookings = bookingRepository.findPastBookingsByBookerId(userId, pageable);
        } else if (bookingState == BookingState.FUTURE) {
            bookings = bookingRepository.findFutureBookingsByBookerId(userId, pageable);
        } else if (bookingState == BookingState.WAITING) {
            bookings = bookingRepository.findBookingsByBookerIdAndBookingStatus(userId, WAITING, pageable);
        } else if (bookingState == BookingState.REJECTED) {
            bookings = bookingRepository.findBookingsByBookerIdAndBookingStatus(userId, REJECTED, pageable);
        } else {
            throw new BadRequestException(String.format("Unknown state: %s", bookingState));
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> findBookingsByItemsOwner(int userId, BookingState bookingState, Integer from, Integer size) {
        findUserById(userRepository, userId);
        PageRequest pageable = PageRequest.of(from > 0 ? from / size : 0, size, bookingStartDateSortDesc);
        List<Booking> bookings;
        if (bookingState == null || bookingState == BookingState.ALL) {
            bookings = bookingRepository.findByOwnerId(userId, pageable);
        } else if (bookingState == BookingState.CURRENT) {
            bookings = bookingRepository.findCurrentBookingsByOwnerId(userId, pageable);
        } else if (bookingState == BookingState.PAST) {
            bookings = bookingRepository.findPastBookingsByOwnerId(userId, pageable);
        } else if (bookingState == BookingState.FUTURE) {
            bookings = bookingRepository.findFutureBookingsByOwnerId(userId, pageable);
        } else if (bookingState == BookingState.WAITING) {
            bookings = bookingRepository.findBookingsByOwnerIdAndBookingStatus(userId, WAITING, pageable);
        } else if (bookingState == BookingState.REJECTED) {
            bookings = bookingRepository.findBookingsByOwnerIdAndBookingStatus(userId, REJECTED, pageable);
        } else {
            throw new BadRequestException(String.format("Unknown state: %s", bookingState));
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private Booking getById(Long bookingId, int userId) {
        findUserById(userRepository, userId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty() || (booking.get().getBooker().getId() != userId &&
                booking.get().getItem().getOwner().getId() != userId)) {
            throw new ObjectNotFoundException(String.format("Бронирование не найдено! Id=%d", bookingId));
        } else {
            return booking.get();
        }
    }
}
