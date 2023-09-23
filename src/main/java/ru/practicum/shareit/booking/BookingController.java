package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingDto> create(@Valid @RequestBody BookingDto bookingDto,
                                             @RequestHeader("X-Sharer-User-Id") int userId) {
        return new ResponseEntity<>(bookingService.create(bookingDto, userId), HttpStatus.CREATED);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> approve(@PathVariable long bookingId, @RequestHeader("X-Sharer-User-Id") int userId,
                                              @RequestParam("approved") boolean approved) {
        return new ResponseEntity<>(bookingService.approve(bookingId, userId, approved), HttpStatus.OK);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> findById(@PathVariable long bookingId, @RequestHeader("X-Sharer-User-Id") int userId) {
        return new ResponseEntity<>(bookingService.findById(bookingId, userId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Object> findBookingsBySearchState(@RequestHeader("X-Sharer-User-Id") int userId,
                                                            @RequestParam(value = "state", required = false, defaultValue = "ALL") String state) {
        return new ResponseEntity<>(bookingService.findBookingsBySearchState(userId, state), HttpStatus.OK);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findBookingsByItemsOwner(@RequestHeader("X-Sharer-User-Id") int userId,
                                                           @RequestParam(value = "state", required = false, defaultValue = "ALL") String state) {
        return new ResponseEntity<>(bookingService.findBookingsByItemsOwner(userId, state), HttpStatus.OK);
    }

}