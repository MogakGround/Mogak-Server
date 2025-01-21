package com.example.mogakserver.roomuser.api.controller;

import com.example.mogakserver.common.exception.dto.ErrorResponse;
import com.example.mogakserver.common.exception.dto.SuccessNonDataResponse;
import com.example.mogakserver.common.exception.enums.SuccessCode;
import com.example.mogakserver.common.util.resolver.user.UserId;
import com.example.mogakserver.room.application.response.ScreenShareUsersListDTO;
import com.example.mogakserver.roomuser.application.service.RoomUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
