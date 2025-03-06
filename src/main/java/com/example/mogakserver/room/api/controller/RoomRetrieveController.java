package com.example.mogakserver.room.api.controller;

import com.example.mogakserver.common.exception.dto.ErrorResponse;
import com.example.mogakserver.common.exception.dto.SuccessNonDataResponse;
import com.example.mogakserver.common.exception.dto.SuccessResponse;
import com.example.mogakserver.common.exception.enums.SuccessCode;
import com.example.mogakserver.common.util.resolver.user.UserId;
import com.example.mogakserver.room.application.response.RoomDTO;
import com.example.mogakserver.room.application.response.RoomListDTO;
import com.example.mogakserver.room.application.response.ScreenShareUsersListDTO;
import com.example.mogakserver.room.application.response.TimerListDTO;
import com.example.mogakserver.room.application.service.RoomRetrieveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomRetrieveController {

    private final RoomRetrieveService roomRetrieveService;

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
        return SuccessResponse.success(SuccessCode.GET_SCREEN_SHARE_USERS_SUCCESS, roomRetrieveService.getScreenShareUsers(roomId, page, size));
    }
    @Operation(summary = "[JWT] 타이머 리스트 조회", description = "해당 방에서 타이머를 실행 중 인 타이머 리스트 조회 api 입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "타이머 리스트 조회 성공", content = @Content(schema = @Schema(implementation = TimerListDTO.class))),
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
        return SuccessResponse.success(SuccessCode.GET_PAGED_TIMERS_SUCCESS, roomRetrieveService.getPagedTimers(roomId,page,size));
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
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) List<String> workHours
    ) {
        RoomListDTO roomListDTO = roomRetrieveService.getAllRooms(page, size, workHours);
        return SuccessResponse.success(SuccessCode.GET_PAGED_ROOMS_SUCCESS, roomListDTO);
    }

    @Operation(summary = "[JWT] 모각방 단일 조회", description = "특정 모각방을 조회하는 API입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "모각방 단일 조회 성공",
                    content = @Content(schema = @Schema(implementation = RoomDTO.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 모각방입니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "JWT Auth")
    @GetMapping("/{roomId}")
    public SuccessResponse<RoomDTO> getRoom(
            @Parameter(hidden = true) @UserId Long userId,
            @PathVariable Long roomId
    ) {
        RoomDTO roomDTO = roomRetrieveService.getRoomById(userId, roomId);
        return SuccessResponse.success(SuccessCode.GET_ROOM_SUCCESS, roomDTO);
    }

    @Operation(summary = "최근 모각방 top4 조회", description = "최근에 생성된 4개의 모각방을 조회하는 API입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최근 모각방 top4 조회 성공",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/recent")
    public SuccessResponse<List<RoomDTO>> getRecentRooms() {
        List<RoomDTO> recentRooms = roomRetrieveService.getRecentRooms();
        return SuccessResponse.success(SuccessCode.GET_RECENT_ROOMS_SUCCESS, recentRooms);
    }


    @Operation(summary = "방 이름 중복 검사", description = "방 이름 중복 검사 API입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용 가능한 방 이름입니다.",
                    content = @Content(schema = @Schema(implementation = SuccessNonDataResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 방 이름입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/check-room-name")
    public SuccessNonDataResponse checkRoomName(@RequestParam String roomName) {
        roomRetrieveService.isRoomNameAvailable(roomName);
        return SuccessNonDataResponse.success(SuccessCode.GET_AVAILABLE_ROOM_NAME);
    }
}
