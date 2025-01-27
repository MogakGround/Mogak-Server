package com.example.mogakserver.room.api.controller;

import com.example.mogakserver.common.exception.dto.ErrorResponse;
import com.example.mogakserver.common.exception.dto.SuccessResponse;
import com.example.mogakserver.common.exception.enums.SuccessCode;
import com.example.mogakserver.common.util.resolver.user.UserId;
import com.example.mogakserver.room.application.response.ScreenShareUsersListDTO;
import com.example.mogakserver.room.application.response.TimerListDTO;
import com.example.mogakserver.room.application.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "[JWT] 화면공유 사용자 리스트 조회", description = "해당 방에서 화면공유 중인 userId 리스트 조회 api 입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "화면공유 사용자 조회 성공", content = @Content(schema = @Schema(implementation = ScreenShareUsersListDTO.class))),
            @ApiResponse(responseCode = "404", description = "유저가 존재하지 않습니다", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @SecurityRequirement(name = "JWT Auth")
    @GetMapping("/{roomId}/screenshare/users")
    public SuccessResponse<ScreenShareUsersListDTO> getScreenShareUsers(
            @Parameter(hidden = true) @UserId Long userId,
            @Parameter(name = "roomId", description = "방 id") @PathVariable(value = "roomId") Long roomId,
            @Parameter(name = "page", description = "페이지 ") @RequestParam(value = "page") int page,
            @Parameter(name = "size", description = "페이지 ") @RequestParam(value = "size") int size
    ) {
        return SuccessResponse.success(SuccessCode.GET_SCREEN_SHARE_USERS_SUCCESS, roomService.getScreenShareUsers(roomId, page, size));
    }
    @Operation(summary = "[JWT] 타이머 리스트 조회", description = "해당 방에서 타이머를 실행 중 인 타이머 리스트 조회 api 입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "타이머 리스트 조회 성공", content = @Content(schema = @Schema(implementation = ScreenShareUsersListDTO.class))),
            @ApiResponse(responseCode = "404", description = "유저가 존재하지 않습니다", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @SecurityRequirement(name = "JWT Auth")
    @GetMapping("/{roomId}/timers")
    public SuccessResponse<TimerListDTO> getTimerList(
            @Parameter(hidden = true) @UserId Long userId,
            @Parameter(name = "roomId", description = "방 id") @PathVariable(value = "roomId") Long roomId,
            @Parameter(name = "page", description = "페이지 ") @RequestParam(value = "page") int page,
            @Parameter(name = "size", description = "페이지 ") @RequestParam(value = "size") int size
    ) {
        return SuccessResponse.success(SuccessCode.GET_PAGED_TIMERS_SUCCESS, roomService.getPagedTimers(roomId,page,size));
    }
}
