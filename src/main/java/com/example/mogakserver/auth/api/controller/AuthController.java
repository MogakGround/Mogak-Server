package com.example.mogakserver.auth.api.controller;

import com.example.mogakserver.auth.api.request.SignUpRequestDto;
import com.example.mogakserver.auth.api.request.TokenRequestDto;
import com.example.mogakserver.auth.application.service.TokenService;
import com.example.mogakserver.common.exception.dto.ErrorResponse;
import com.example.mogakserver.common.exception.dto.SuccessResponse;
import com.example.mogakserver.common.exception.dto.TokenPair;
import com.example.mogakserver.common.exception.enums.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.mogakserver.auth.application.response.LoginResponseDto;
import com.example.mogakserver.auth.application.service.AuthService;
import java.util.Map;
import static com.example.mogakserver.common.exception.enums.SuccessCode.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String ORIGIN = "origin";
    private final AuthService authService;
    private final TokenService tokenService;

    @Operation(summary = "카카오 로그인 API", description = "카카오 로그인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카카오 로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 카카오 코드를 입력했습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public SuccessResponse<LoginResponseDto> login(
            @RequestBody @Schema(description = "카카오 인증 코드", example = "{\"kakaoCode\": \"string\"}") Map<String, String> body,
            HttpServletRequest request) {
        String kakaoCode = body.get("kakaoCode");
        if (kakaoCode == null || kakaoCode.isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.EMPTY_KAKAO_CODE_EXCEPTION.getMessage());
        }

        String originHeader = request.getHeader(ORIGIN);
        LoginResponseDto responseDto = authService.login(originHeader, kakaoCode);

        if (responseDto.isNewUser()) {
            return SuccessResponse.success(SIGNUP_SUCCESS, responseDto);
        }
        return SuccessResponse.success(SOCIAL_LOGIN_SUCCESS, responseDto);
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
            @RequestBody SignUpRequestDto signUpRequest) {
        if (signUpRequest == null || signUpRequest.kakaoId() == null) {
            throw new IllegalArgumentException(ErrorCode.VALIDATION_REQUEST_MISSING_EXCEPTION.getMessage());
        }
        return SuccessResponse.success(SOCIAL_LOGIN_SUCCESS, authService.signUp(signUpRequest));
    }


    @Operation(summary = "로그아웃 API", description = "사용자 로그아웃")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    public SuccessResponse<Void> logout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        String token = authorizationHeader.replace("Bearer ", "");
        Long userId = authService.extractUserIdFromToken(token);
        authService.logout(userId);
        return SuccessResponse.success(LOGOUT_SUCCESS, null);
    }


    @Operation(summary = "토큰 갱신 API", description = "리프레시 토큰 발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = TokenPair.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 리프레시 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public SuccessResponse<TokenPair> refresh(@RequestBody final TokenRequestDto tokenRequestDto) {
        if (tokenRequestDto == null || tokenRequestDto.refreshToken() == null) {
            throw new IllegalArgumentException(ErrorCode.VALIDATION_REQUEST_MISSING_EXCEPTION.getMessage());
        }
        return SuccessResponse.success(REFRESH_SUCCESS, authService.refresh(tokenRequestDto));
    }


    @Operation(summary = "회원 탈퇴 API", description = "사용자 계정 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/delete")
    public SuccessResponse<Void> deleteUser(HttpServletRequest request) {
            String authorizationHeader = request.getHeader("Authorization");

            String token = authorizationHeader.replace("Bearer ", "");
            Long userId = authService.extractUserIdFromToken(token);
            authService.deleteUser(userId);

        return SuccessResponse.success(USER_DELETION_SUCCESS, null);

    }
}

