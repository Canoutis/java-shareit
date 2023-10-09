package ru.practicum.shareit.item;

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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.exception.ObjectAccessException;
import ru.practicum.shareit.handler.ErrorHandler;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {
    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;

    private ItemDto itemDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .setControllerAdvice(ErrorHandler.class)
                .build();

        userDto = new UserDto(
                1,
                "test@etcdev.ru",
                "First Test");

        itemDto = new ItemDto(
                1L,
                "Перфоратор",
                "Электрический",
                true,
                1, null);
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testSaveNewItem() throws Exception {
        Mockito.when(itemService.create(anyInt(), any(ItemDto.class)))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.ownerId", is(itemDto.getOwnerId())));
    }

    @Test
    void testUpdateItem() throws Exception {
        ItemDto updatedItemDto = new ItemDto(
                1L,
                "Перфоратор мощный",
                "Электрический, беспроводной",
                true,
                1, null);

        Mockito.when(itemService.update(anyInt(), anyLong(), any(ItemDto.class)))
                .thenReturn(updatedItemDto);

        mvc.perform(patch("/items/{id}", itemDto.getId())
                        .content(mapper.writeValueAsString(updatedItemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(updatedItemDto.getName())))
                .andExpect(jsonPath("$.description", is(updatedItemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(updatedItemDto.getAvailable())))
                .andExpect(jsonPath("$.ownerId", is(updatedItemDto.getOwnerId())));
    }

    @Test
    void testGetItemById() throws Exception {

        Mockito.when(itemService.getItemById(anyLong(), anyInt()))
                .thenReturn(ItemMapper.toOwnerItemDto(ItemMapper.toItemEntity(itemDto, UserMapper.toUserEntity(userDto), null)));

        mvc.perform(get("/items/{id}", itemDto.getId())
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.ownerId", is(itemDto.getOwnerId())));
    }

    @Test
    void testItemByOwnerId() throws Exception {

        Mockito.when(itemService.getItemsByOwnerId(anyInt(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(ItemMapper.toOwnerItemDto(ItemMapper.toItemEntity(itemDto,
                        UserMapper.toUserEntity(userDto), null))));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[0].ownerId", is(itemDto.getOwnerId())));
    }

    @Test
    void testSearchItems() throws Exception {

        Mockito.when(itemService.searchItems(anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(ItemMapper.toOwnerItemDto(ItemMapper.toItemEntity(itemDto,
                        UserMapper.toUserEntity(userDto), null))));

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "Перфоратор")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[0].ownerId", is(itemDto.getOwnerId())));
    }

    @Test
    void testSaveComment() throws Exception {

        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Хороший перфоратор")
                .authorName("Second Test2")
                .created(LocalDateTime.now())
                .build();
        Mockito.when(itemService.saveComment(commentDto, 2, itemDto.getId()))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", itemDto.getId())
                        .content(mapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is("Second Test2")));
    }


    @Test
    void testApproveBookingBadRequestException() throws Exception {
        ItemDto updatedItemDto = new ItemDto(
                1L,
                "Перфоратор мощный",
                "Электрический, беспроводной",
                true,
                1, null);
        Mockito.when(itemService.update(anyInt(), anyLong(), any(ItemDto.class)))
                .thenThrow(ObjectAccessException.class);

        mvc.perform(patch("/items/{id}", itemDto.getId())
                        .content(mapper.writeValueAsString(updatedItemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(403));
    }
}
