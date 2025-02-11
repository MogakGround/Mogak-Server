package com.example.mogakserver.roomuser.api.controller;

import com.example.mogakserver.common.exception.dto.ErrorResponse;
import com.example.mogakserver.common.exception.dto.SuccessNonDataResponse;
import com.example.mogakserver.common.exception.dto.SuccessResponse;
import com.example.mogakserver.common.exception.enums.SuccessCode;
import com.example.mogakserver.common.util.resolver.user.UserId;
import com.example.mogakserver.room.application.response.ScreenShareUsersListDTO;
import com.example.mogakserver.roomuser.application.response.MyStatusResponseDTO;
import com.example.mogakserver.roomuser.application.response.UserRoomsListDTO;
import com.example.mogakserver.roomuser.application.response.RoomUserListDTO;
import com.example.mogakserver.roomuser.application.service.RoomUserService;
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
public class RoomUserController {
    private final RoomUserService roomUserService;
    @Operation(summary = "[JWT] 화면공유 확대 허용 여부 변경", description = "해당 방에서 사용자의 화면 확대 허용 여부 변경 api 입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "화면공유 확대 여부 변경 성공", content = @Content(schema = @Schema(implementation = ScreenShareUsersListDTO.class))),
            @ApiResponse(responseCode = "404", description = "유저가 존재하지 않습니다", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @SecurityRequirement(name = "JWT Auth")
    @GetMapping("/{roomId}/screenshare/large")
    public SuccessNonDataResponse updateIsScreenLargeAllowed(
            @Parameter(hidden = true) @UserId Long userId,
            @Parameter(name = "roomId", description = "방 id") @PathVariable(value = "roomId") Long roomId
    ) {
        roomUserService.updateIsScreenShareLargeAllowed(userId, roomId);
        return SuccessNonDataResponse.success(SuccessCode.UPDATE_SCREEN_SHARE_LARGE_SUCCESS);
    }
    @Operation(summary = "[JWT] 모각작 방 안에서의 내 상태 조회", description = "해당 방에서 사용자의 화면공유, 타이머 등의 상태 조회 api입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "모각작 방에서 내 상태 조회 성공", content = @Content(schema = @Schema(implementation = ScreenShareUsersListDTO.class))),
            @ApiResponse(responseCode = "404", description = "유저가 존재하지 않습니다", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @SecurityRequirement(name = "JWT Auth")
    @GetMapping("/{roomId}/mystatus")
    public SuccessResponse<MyStatusResponseDTO> getMyStatusInRoom(
            @Parameter(hidden = true) @UserId Long userId,
            @Parameter(name = "roomId", description = "방 id") @PathVariable(value = "roomId") Long roomId
    ) {
        MyStatusResponseDTO response = roomUserService.getStatus(userId, roomId);
        return SuccessResponse.success(SuccessCode.GET_MY_STATUS_IN_ROOM_SUCCESS, response);
    }

    @Operation(summary = "[JWT] 내가 만든 모각방 리스트 조회", description = "내가 만든 모각방 리스트 조회api입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내가 만든 모각방 리스트 조회 성공", content = @Content(schema = @Schema(implementation = ScreenShareUsersListDTO.class))),
            @ApiResponse(responseCode = "404", description = "유저가 존재하지 않습니다", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @SecurityRequirement(name = "JWT Auth")
    @GetMapping("/mypage/rooms/imade")
    public SuccessResponse<UserRoomsListDTO> getRoomsMadeByMeList(
            @Parameter(hidden = true) @UserId Long userId,
            @Parameter(name = "page", description = "페이지 ") @RequestParam(value = "page") int page,
            @Parameter(name = "size", description = "페이지 ") @RequestParam(value = "size") int size
    ) {
        return SuccessResponse.success(SuccessCode.GET_ROOMS_I_MADE_SUCCESS, roomUserService.getRoomListIMade(userId, page, size));
    }

    @Operation(summary = "[JWT] 모각방 참가 인원 명단 조회", description = "특정 모각방에 속한 사용자 명단을 조회하는 API입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "모각방 명단 조회 성공",
                    content = @Content(schema = @Schema(implementation = RoomUserListDTO.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 모각방입니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "JWT Auth")
    @GetMapping("/{roomId}/participants")
    public SuccessResponse<RoomUserListDTO> getRoomUsers(
            @Parameter(hidden = true) @UserId Long userId,
            @PathVariable Long roomId
    ) {
        RoomUserListDTO roomUserList = roomUserService.getRoomUsers(userId, roomId);
        return SuccessResponse.success(SuccessCode.GET_ROOM_USERS_SUCCESS, roomUserList);
    }
}
