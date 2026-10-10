package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.it.ItPlaygroundExecutionRequest;
import com.englishlearning.dto.it.ItPlaygroundExecutionResponse;
import com.englishlearning.service.ItPlaygroundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/it/playground")
@RequiredArgsConstructor
public class ItPlaygroundController {

    private final ItPlaygroundService playgroundService;

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<ItPlaygroundExecutionResponse>> execute(
            @Valid @RequestBody ItPlaygroundExecutionRequest request
    ) {
        ItPlaygroundExecutionResponse response = playgroundService.executeOrAnalyze(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
