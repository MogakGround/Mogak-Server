package com.example.mogakserver.user.api.controller;

import com.example.mogakserver.common.exception.dto.ErrorResponse;
import com.example.mogakserver.common.exception.dto.SuccessResponse;
import com.example.mogakserver.common.exception.enums.SuccessCode;
import com.example.mogakserver.common.util.resolver.user.UserId;
import com.example.mogakserver.room.application.response.ScreenShareUsersListDTO;
import com.example.mogakserver.user.application.response.MyProfileResponseDTO;
import com.example.mogakserver.user.application.response.RankingDTO;
import com.example.mogakserver.user.application.response.RankingListDTO;
import com.example.mogakserver.user.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "[JWT] 랭킹 리스트 조회", description = "랭킹 리스트 조회 api 입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "랭킹 리스트 조회 성공", content = @Content(schema = @Schema(implementation = ScreenShareUsersListDTO.class))),
            @ApiResponse(responseCode = "404", description = "유저가 존재하지 않습니다", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @SecurityRequirement(name = "JWT Auth")
    @GetMapping("/rankings")
    public SuccessResponse<RankingListDTO> getRankingList(
            @Parameter(hidden = true, required = false) @UserId Long userId,
            @Parameter(name = "page", description = "페이지 ") @RequestParam(value = "page") int page,
            @Parameter(name = "size", description = "페이지 ") @RequestParam(value = "size") int size
    ) {
        return SuccessResponse.success(SuccessCode.GET_RANKING_SUCCESS, userService.getPagedRanking(page, size));
    }
    @Operation(summary = "[JWT] 내 랭킹 조회", description = "내 랭킹 조회 api 입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "랭킹 조회 성공", content = @Content(schema = @Schema(implementation = ScreenShareUsersListDTO.class))),
            @ApiResponse(responseCode = "404", description = "유저가 존재하지 않습니다", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @SecurityRequirement(name = "JWT Auth")
    @GetMapping("/my/ranking")
    public SuccessResponse<RankingDTO> getMyranking(
            @Parameter(hidden = true) @UserId Long userId
    ) {
        return SuccessResponse.success(SuccessCode.GET_RANKING_SUCCESS, userService.getUserRanking(userId));
    }

    @Operation(summary = "[JWT] 내 프로필 조회", description = "내 프로필 조회 api 입니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공", content = @Content(schema = @Schema(implementation = ScreenShareUsersListDTO.class))),
            @ApiResponse(responseCode = "404", description = "유저가 존재하지 않습니다", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @SecurityRequirement(name = "JWT Auth")
    @GetMapping("/my/profile")
    public SuccessResponse<MyProfileResponseDTO> getMyProfile(
            @Parameter(hidden = true) @UserId Long userId
    ) {
        return SuccessResponse.success(SuccessCode.GET_RANKING_SUCCESS, userService.getUserProfile(userId));
    }

}
