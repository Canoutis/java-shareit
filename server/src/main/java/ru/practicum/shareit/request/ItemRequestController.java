package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ResponseEntity<ItemRequestDto> create(@RequestHeader("X-Sharer-User-Id") int userId,
                                                 @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return new ResponseEntity<>(itemRequestService.create(userId, itemRequestDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Collection<ItemRequestDto>> findUserItemRequests(@RequestHeader("X-Sharer-User-Id") int userId) {
        return new ResponseEntity<>(itemRequestService.findUserItemRequests(userId), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<Collection<ItemRequestDto>> findOtherItemRequests(@RequestHeader("X-Sharer-User-Id") int userId,
                                                                            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                                            @Positive @RequestParam(defaultValue = "20") Integer size) {
        return new ResponseEntity<>(itemRequestService.findOtherItemRequests(userId, from, size), HttpStatus.OK);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> findById(@RequestHeader("X-Sharer-User-Id") int userId,
                                                   @PathVariable Long requestId) {
        return new ResponseEntity<>(itemRequestService.findById(userId, requestId), HttpStatus.OK);
    }

}