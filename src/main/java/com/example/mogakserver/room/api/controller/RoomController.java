package com.example.mogakserver.room.api.controller;

import com.example.mogakserver.common.exception.dto.ErrorResponse;
import com.example.mogakserver.common.exception.dto.SuccessNonDataResponse;
import com.example.mogakserver.common.exception.dto.SuccessResponse;
import com.example.mogakserver.common.exception.enums.SuccessCode;
import com.example.mogakserver.common.util.resolver.user.UserId;
import com.example.mogakserver.room.application.dto.RoomRequestDTO;
import com.example.mogakserver.room.application.dto.RoomUpdateDTO;
import com.example.mogakserver.room.application.response.RoomListDTO;
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

import java.util.List;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @Operation(summary = "[JWT] 모각방 생성", description = "새로운 모각방을 생성하는 API입니다")
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
    public SuccessNonDataResponse createRoom(
            @Parameter(hidden = true) @UserId Long userId,
            @Valid @RequestBody RoomRequestDTO roomRequestDTO
    ) {
        roomService.createRoom(userId, roomRequestDTO);
        return SuccessNonDataResponse.success(SuccessCode.ROOM_CREATION_SUCCESS);
    }

    @Operation(summary = "[JWT] 모각방 정보 수정", description = "모각방 정보를 수정하는 API입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "모각방 정보 수정 성공",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 모각방입니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "JWT Auth")
    @PatchMapping("/{roomId}")
    public SuccessNonDataResponse updateRoom(
            @Parameter(hidden = true) @UserId Long userId,
            @PathVariable Long roomId,
            @Valid @RequestBody RoomUpdateDTO roomUpdateDTO
    ) {
        roomService.updateRoom(userId, roomId, roomUpdateDTO);
        return SuccessNonDataResponse.success(SuccessCode.UPDATE_ROOM_SUCCESS);
    }

    @Operation(summary = "모각방 전체 조회", description = "모든 모각방을 조회하는 API입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "모각방 전체 조회 성공",
                    content = @Content(schema = @Schema(implementation = RoomListDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public SuccessResponse<RoomListDTO> getAllRooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) List<String> workHours
    ) {
        RoomListDTO roomListDTO = roomService.getAllRooms(page, workHours);
        return SuccessResponse.success(SuccessCode.GET_PAGED_ROOMS_SUCCESS, roomListDTO);
    }
}