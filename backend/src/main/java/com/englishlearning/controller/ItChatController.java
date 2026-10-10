package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.it.ItChatSessionDto;
import com.englishlearning.dto.it.SendItChatRequest;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.ItChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/it/chat")
@RequiredArgsConstructor
public class ItChatController {

    private final ItChatService chatService;

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ItChatSessionDto>>> getSessions(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<ItChatSessionDto> sessions = chatService.getSessions(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(sessions));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<ItChatSessionDto>> getSessionById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long sessionId
    ) {
        ItChatSessionDto session = chatService.getSessionById(userPrincipal.getId(), sessionId);
        return ResponseEntity.ok(ApiResponse.ok(session));
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ItChatSessionDto>> sendMessage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SendItChatRequest request
    ) {
        ItChatSessionDto session = chatService.sendMessage(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(session));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long sessionId
    ) {
        chatService.deleteSession(userPrincipal.getId(), sessionId);
        return ResponseEntity.ok(ApiResponse.<Void>ok("Đã xóa phiên chat", null));
    }
}
