package com.example.mogakserver.auth.api.controller;

import com.example.mogakserver.auth.api.request.LoginRequestDTO;
import com.example.mogakserver.auth.api.request.SignUpRequestDTO;
import com.example.mogakserver.auth.api.request.TokenRequestDTO;
import com.example.mogakserver.common.exception.dto.ErrorResponse;
import com.example.mogakserver.auth.application.response.LoginResponseDto;
import com.example.mogakserver.auth.application.service.AuthService;
import com.example.mogakserver.common.exception.dto.SuccessResponse;
import com.example.mogakserver.common.exception.dto.TokenPair;
import com.example.mogakserver.common.util.resolver.user.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.mogakserver.common.exception.enums.SuccessCode.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "[KAKAO CODE] 로그인 API", description = "카카오 로그인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카카오 로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 카카오 코드를 입력했습니다.", content = @ Content(schema = @ Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 유저는 존재하지 않습니다.", content = @ Content(schema = @ Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @ Content(schema = @ Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public SuccessResponse<LoginResponseDto> login(@RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDto loginResponse = authService.login(loginRequest.kakaoCode());

        if ("fail".equals(loginResponse.status())) {
            return SuccessResponse.success(SIGNUP_REQUIRED, loginResponse);
        }

        return SuccessResponse.success(SOCIAL_LOGIN_SUCCESS, loginResponse);
    }


    @Operation(summary = "회원가입 API", description = "카카오 로그인 사용자 회원가입")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public SuccessResponse<LoginResponseDto> signUp(
            @RequestBody SignUpRequestDTO signUpRequest) {
        return SuccessResponse.success(SOCIAL_LOGIN_SUCCESS, authService.signUp(signUpRequest));
    }


    @Operation(summary = "[JWT] 로그아웃 API", description = "사용자 로그아웃")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "JWT Auth")
    @PostMapping("/logout")
    public SuccessResponse<Void> logout(@Parameter(hidden = true) @UserId Long userId) {
        authService.logout(userId);
        return SuccessResponse.success(LOGOUT_SUCCESS, null);
    }

    @Operation(summary = "[JWT] 토큰 갱신 API", description = "리프레시 토큰 갱신")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = TokenPair.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 리프레시 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public SuccessResponse<TokenPair> refresh(@RequestBody final TokenRequestDTO tokenRequestDto) {
        return SuccessResponse.success(REFRESH_SUCCESS, authService.refresh(tokenRequestDto));
    }


    @Operation(summary = "[JWT] 회원 탈퇴 API", description = "사용자 회원 탈퇴")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "회원 인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/delete")
    @SecurityRequirement(name = "JWT Auth")
    public SuccessResponse<Void> deleteUser(@Parameter(hidden = true) @UserId Long userId) {
        authService.deleteUser(userId);
        return SuccessResponse.success(USER_DELETION_SUCCESS, null);
    }
}

