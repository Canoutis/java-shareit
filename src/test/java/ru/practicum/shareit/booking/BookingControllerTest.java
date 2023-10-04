package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.Booking.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.Booking.BookingStatus.WAITING;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {
    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;

    private BookingDto bookingDto;
    private BookItemRequestDto bookItemRequestDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .build();

        UserDto bookerDto = new UserDto(
                2,
                "test2@etcdev.ru",
                "Second Test2");

        ItemDto itemDto = new ItemDto(
                1L,
                "Перфоратор",
                "Электрический",
                true,
                1, null);
        bookingDto = new BookingDto(1L,
                Booking.BookingStatus.WAITING,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(8),
                itemDto,
                bookerDto
        );
        bookItemRequestDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(8),
                itemDto.getId(),
                bookerDto.getId()
        );

        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testCreateBooking() throws Exception {
        Mockito.when(bookingService.create(any(BookItemRequestDto.class), anyInt()))
                .thenReturn(bookingDto);


        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookItemRequestDto))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void testApproveBooking() throws Exception {
        bookingDto.setStatus(APPROVED);
        Mockito.when(bookingService.approve(1L, 1, true))
                .thenReturn(bookingDto);


        mvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is(APPROVED.toString())));
    }

    @Test
    void testFindBookingById() throws Exception {
        Mockito.when(bookingService.findById(1L, 2))
                .thenReturn(bookingDto);


        mvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is(WAITING.toString())));
    }

    @Test
    void testFindBookingsBySearchStateALL() throws Exception {
        Mockito.when(bookingService.findBookingsBySearchState(2, State.ALL, 0, 2))
                .thenReturn(Collections.singletonList(bookingDto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is(WAITING.toString())));
    }

    @Test
    void testFindBookingsBySearchStatePAST() throws Exception {
        Mockito.when(bookingService.findBookingsBySearchState(2, State.PAST, 0, 2))
                .thenReturn(new ArrayList<>());

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .param("state", "PAST")
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testFindBookingsBySearchStateFUTURE() throws Exception {
        Mockito.when(bookingService.findBookingsBySearchState(2, State.FUTURE, 0, 2))
                .thenReturn(Collections.singletonList(bookingDto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .param("state", "FUTURE")
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is(WAITING.toString())));
    }

    @Test
    void testFindBookingsByItemsOwnerStateALL() throws Exception {
        Mockito.when(bookingService.findBookingsByItemsOwner(1, State.ALL, 0, 2))
                .thenReturn(Collections.singletonList(bookingDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is(WAITING.toString())));
    }

    @Test
    void testFindBookingsByItemsOwnerStatePAST() throws Exception {
        Mockito.when(bookingService.findBookingsByItemsOwner(1, State.PAST, 0, 2))
                .thenReturn(new ArrayList<>());

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "PAST")
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testFindBookingsByItemsOwnerStateFUTURE() throws Exception {
        Mockito.when(bookingService.findBookingsByItemsOwner(1, State.FUTURE, 0, 2))
                .thenReturn(Collections.singletonList(bookingDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "FUTURE")
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is(WAITING.toString())));
    }

}
