package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.game.*;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GameDto>>> getGames() {
        List<GameDto> response = gameService.getAvailableGames();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{code}/start")
    public ResponseEntity<ApiResponse<GameSessionStartResponse>> startGame(
            @PathVariable String code,
            @RequestParam(required = false) Long topicId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        GameSessionStartResponse response = gameService.startGameSession(userPrincipal.getId(), code, topicId);
        return ResponseEntity.ok(ApiResponse.ok("Bắt đầu phiên chơi game thành công", response));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<GameResultResponse>> submitAnswers(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody GameAnswerSubmitRequest request) {
        GameResultResponse response = gameService.submitGameAnswers(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Kết quả trò chơi đã được lưu", response));
    }
}
