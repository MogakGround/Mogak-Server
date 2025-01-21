package com.example.mogakserver.external.socket.dto;

public record MessageDTO(
        String type,
        Long userId
) {
}
