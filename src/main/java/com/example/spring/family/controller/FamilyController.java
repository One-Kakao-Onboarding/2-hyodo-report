package com.example.spring.family.controller;

import com.example.spring.common.dto.ApiResponse;
import com.example.spring.family.dto.CreateFamilyRequest;
import com.example.spring.family.dto.FamilyResponse;
import com.example.spring.family.dto.JoinFamilyRequest;
import com.example.spring.family.service.FamilyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 가족 그룹 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/families")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    /**
     * 가족 그룹 생성
     * POST /api/families
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FamilyResponse>> createFamily(
            @Valid @RequestBody CreateFamilyRequest request,
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        FamilyResponse response = familyService.createFamily(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "가족 그룹이 생성되었습니다."));
    }

    /**
     * 초대 코드로 가족 그룹 가입
     * POST /api/families/join
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<FamilyResponse>> joinFamily(
            @Valid @RequestBody JoinFamilyRequest request,
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        FamilyResponse response = familyService.joinFamily(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "가족 그룹에 가입되었습니다."));
    }

    /**
     * 내가 속한 모든 가족 그룹 조회
     * GET /api/families/my
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<FamilyResponse>>> getMyFamilies(
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        List<FamilyResponse> response = familyService.getMyFamilies(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 가족 그룹 상세 조회
     * GET /api/families/{familyId}
     */
    @GetMapping("/{familyId}")
    public ResponseEntity<ApiResponse<FamilyResponse>> getFamily(
            @PathVariable Long familyId,
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        FamilyResponse response = familyService.getFamily(familyId, userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 가족 그룹 탈퇴
     * DELETE /api/families/{familyId}/leave
     */
    @DeleteMapping("/{familyId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveFamily(
            @PathVariable Long familyId,
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        familyService.leaveFamily(familyId, userId);

        return ResponseEntity.ok(ApiResponse.success(null, "가족 그룹에서 탈퇴했습니다."));
    }
}
