package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.ObjectAccessException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ObjectSaveException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.OwnerItemDto;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.utils.Helper.findUserById;

@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository,
                           ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    @Transactional
    public ItemDto create(Integer userId, ItemDto itemDto) {
        Optional<User> owner = userRepository.findById(userId);
        if (owner.isEmpty()) {
            throw new ObjectNotFoundException(String.format("Пользователь не найден! Id=%d", userId));
        }
        itemDto.setOwnerId(userId);
        return ItemMapper.toItemDto(itemRepository.save(ItemMapper.toItemEntity(itemDto, owner.get(),
                getItemRequestById(itemDto.getRequestId()))));
    }

    @Override
    @Transactional
    public ItemDto update(Integer userId, Long itemId, ItemDto itemDto) {
        User user = findUserById(userRepository, userId);
        ItemDto tempItemDto = getItemById(itemId, userId);
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
        Item item = ItemMapper.toItemEntity(tempItemDto, user, getItemRequestById(itemDto.getRequestId()));
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public Collection<OwnerItemDto> getItemsByOwnerId(int userId, Integer from, Integer size) {
        findUserById(userRepository, userId);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        PageRequest pageable = PageRequest.of(from > 0 ? from / size : 0, size, sort);
        List<Item> items = itemRepository.findByOwnerIdIs(userId, pageable);
        Map<Long, OwnerItemDto> itemMap = items.stream()
                .collect(Collectors.toMap(Item::getId, ItemMapper::toOwnerItemDto));

        List<Booking> bookings = bookingRepository
                .findApprovedBookings(itemMap.keySet(), Sort.by(Sort.Direction.DESC, "endDate"));

        LocalDateTime now = LocalDateTime.now();
        for (Booking booking : bookings) {
            if (booking.getEndDate().isBefore(now) &&
                    (itemMap.get(booking.getItem().getId()).getLastBooking() == null ||
                            itemMap.get(booking.getItem().getId())
                                    .getLastBooking().getEnd()
                                    .isBefore(booking.getEndDate()))) {
                itemMap.get(booking.getItem().getId()).setLastBooking(BookingMapper.toBookingRequestDto(booking));
            } else if (booking.getStartDate().isAfter(now) &&
                    (itemMap.get(booking.getItem().getId()).getNextBooking() == null ||
                            itemMap.get(booking.getItem().getId())
                                    .getNextBooking().getStart()
                                    .isAfter(booking.getStartDate()))) {
                itemMap.get(booking.getItem().getId()).setNextBooking(BookingMapper.toBookingRequestDto(booking));
            }
        }
        List<Comment> comments = commentRepository.findCommentsByItems(itemMap.keySet(), Sort.by(Sort.Direction.DESC, "created"));
        for (Comment comment : comments) {
            itemMap.get(comment.getItem().getId()).getComments().add(CommentMapper.toCommentDto(comment));
        }
        return itemMap.values();
    }

    @Override
    @Transactional
    public List<ItemDto> searchItems(String text, Integer from, Integer size) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        Sort sort = Sort.by("id");
        PageRequest pageable = PageRequest.of(from > 0 ? from / size : 0, size, sort);
        List<Item> items = itemRepository.findItemsByText(text, pageable);
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public OwnerItemDto getItemById(long itemId, Integer userId) {
        findUserById(userRepository, userId);
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) {
            throw new ObjectNotFoundException(String.format("Вещь не найдена! Id=%d", itemId));
        } else {
            OwnerItemDto ownerItemDto = ItemMapper.toOwnerItemDto(item.get());
            if (userId != null && item.get().getOwner().getId() == userId.intValue()) {
                List<Booking> lastBooking = bookingRepository.findOwnerLastBooking(item.get().getId(), Sort.by(Sort.Direction.DESC, "endDate"));
                List<Booking> nextBooking = bookingRepository.findOwnerNextBooking(item.get().getId(), Sort.by("startDate"));
                ownerItemDto.setLastBooking(!lastBooking.isEmpty() ? BookingMapper.toBookingRequestDto(lastBooking.get(0)) : null);
                ownerItemDto.setNextBooking(!nextBooking.isEmpty() ? BookingMapper.toBookingRequestDto(nextBooking.get(0)) : null);
            } else {
                ownerItemDto = new OwnerItemDto(ItemMapper.toItemDto(item.get()));
            }
            ownerItemDto.getComments().addAll(commentRepository.findByItem_IdIsOrderByCreatedDesc(itemId)
                    .stream().map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList()));
            return ownerItemDto;
        }
    }

    @Override
    @Transactional
    public CommentDto saveComment(CommentDto commentDto, int userId, long itemId) {
        User author = findUserById(userRepository, userId);
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) throw new ObjectNotFoundException(String.format("Вещь не найдена! Id=%x", itemId));
        List<Booking> bookings = bookingRepository.findExpiredByBookerIdAndItemId(userId, itemId);
        if (bookings != null && !bookings.isEmpty()) {
            Comment comment = CommentMapper.toCommentEntity(commentDto, author, item.get());
            return CommentMapper.toCommentDto(commentRepository.save(comment));
        } else {
            throw new ObjectSaveException("Вы не можете оставить отзыв этому товару!");
        }
    }

    @Nullable
    private ItemRequest getItemRequestById(Long id) {
        ItemRequest itemRequest = null;
        if (id != null) {
            Optional<ItemRequest> tempItemRequest = itemRequestRepository.findById(id);
            if (tempItemRequest.isEmpty())
                throw new ObjectNotFoundException(String.format("Запрос не найден! Id=%d", id));
            else
                itemRequest = tempItemRequest.get();
        }
        return itemRequest;
    }

}
