package ru.practicum.shareit.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    String error;
    String description;

    public ErrorResponse(String error) {
        this.error = error;
        this.description = "";
    }

}
