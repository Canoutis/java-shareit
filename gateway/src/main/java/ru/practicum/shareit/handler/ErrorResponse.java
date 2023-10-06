package ru.practicum.shareit.handler;

import lombok.Getter;

@Getter
public class ErrorResponse {
    String error;
    String description;

    public ErrorResponse(String error) {
        this.error = error;
        this.description = "";
    }

}
