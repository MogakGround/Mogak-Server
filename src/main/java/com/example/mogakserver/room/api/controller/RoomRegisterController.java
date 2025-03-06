package com.example.mogakserver.room.api.controller;

import com.example.mogakserver.common.exception.dto.ErrorResponse;
import com.example.mogakserver.common.exception.dto.SuccessNonDataResponse;
import com.example.mogakserver.common.exception.dto.SuccessResponse;
import com.example.mogakserver.common.exception.enums.SuccessCode;
import com.example.mogakserver.common.util.resolver.user.UserId;
import com.example.mogakserver.room.api.request.RoomRequestDTO;
import com.example.mogakserver.room.api.request.RoomUpdateDTO;
import com.example.mogakserver.room.application.service.RoomRegisterService;
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
public class RoomRegisterController {
    private final RoomRegisterService roomRegisterService;

    @Operation(summary = "[JWT] 모각방 생성", description = "새로운 모각방을 생성하는 API입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "모각방 생성 성공",
                    content = @Content(schema = @Schema(implementation = SuccessNonDataResponse.class))),
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
        roomRegisterService.createRoom(userId, roomRequestDTO);
        return SuccessNonDataResponse.success(SuccessCode.ROOM_CREATION_SUCCESS);
    }

    @Operation(summary = "[JWT] 모각방 정보 수정", description = "모각방 정보를 수정하는 API입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "모각방 정보 수정 성공",
                    content = @Content(schema = @Schema(implementation = SuccessNonDataResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "방 관련 권한이 없습니다",
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
        roomRegisterService.updateRoom(userId, roomId, roomUpdateDTO);
        return SuccessNonDataResponse.success(SuccessCode.UPDATE_ROOM_SUCCESS);
    }

    @Operation(summary = "[JWT] 모각방 삭제", description = "모각방을 삭제하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "모각방 삭제 성공",
                    content = @Content(schema = @Schema(implementation = SuccessNonDataResponse.class))),
            @ApiResponse(responseCode = "403", description = "방 관련 권한이 없습니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 모각방입니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "JWT Auth")
    @DeleteMapping("/{roomId}")
    public SuccessNonDataResponse deleteRoom(
            @Parameter(hidden = true) @UserId Long userId,
            @PathVariable Long roomId
    ) {
        roomRegisterService.deleteRoom(userId, roomId);
        return SuccessNonDataResponse.success(SuccessCode.ROOM_DELETION_SUCCESS);
    }
}