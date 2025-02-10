package com.example.mogakserver.room.api.controller;

import com.example.mogakserver.common.exception.dto.ErrorResponse;
import com.example.mogakserver.common.exception.dto.SuccessResponse;
import com.example.mogakserver.common.exception.enums.SuccessCode;
import com.example.mogakserver.common.util.resolver.user.UserId;
import com.example.mogakserver.room.application.dto.RoomRequestDTO;
import com.example.mogakserver.room.application.response.RoomDTO;
import com.example.mogakserver.room.application.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @Operation(summary = "[JWT] 모각 방 생성", description = "새로운 모각방을 생성하는 API입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "모각방 생성 성공",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "JWT Auth")
    @PostMapping
    public SuccessResponse<RoomDTO> createRoom(
            @Parameter(hidden = true) @UserId Long userId,
            @Valid @RequestBody RoomRequestDTO roomRequestDTO
    ) {
        Long roomId = roomService.createRoom(userId, roomRequestDTO);
        return SuccessResponse.success(SuccessCode.ROOM_CREATION_SUCCESS, new RoomDTO(roomId));
    }
}