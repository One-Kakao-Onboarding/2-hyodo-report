package com.example.spring.family.service;

import com.example.spring.family.domain.Family;
import com.example.spring.family.domain.FamilyMember;
import com.example.spring.family.dto.CreateFamilyRequest;
import com.example.spring.family.dto.FamilyResponse;
import com.example.spring.family.dto.JoinFamilyRequest;
import com.example.spring.family.repository.FamilyMemberRepository;
import com.example.spring.family.repository.FamilyRepository;
import com.example.spring.user.domain.User;
import com.example.spring.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 가족 그룹 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;

    /**
     * 가족 그룹 생성
     * @param userId 생성하는 사용자 ID
     * @param request 생성 요청
     * @return 생성된 가족 그룹 정보
     */
    @Transactional
    public FamilyResponse createFamily(Long userId, CreateFamilyRequest request) {
        log.info("Creating family group. userId: {}, name: {}", userId, request.name());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + userId));

        // 가족 그룹 생성
        Family family = Family.builder()
                .name(request.name())
                .build();

        // 생성자를 가족 구성원으로 추가
        FamilyMember member = FamilyMember.builder()
                .user(user)
                .family(family)
                .role(request.role())
                .nickname(request.nickname())
                .build();

        family.addMember(member);
        familyRepository.save(family);

        log.info("Family group created successfully. familyId: {}, inviteCode: {}",
                family.getId(), family.getInviteCode());

        return FamilyResponse.from(family);
    }

    /**
     * 초대 코드로 가족 그룹 가입
     * @param userId 가입하는 사용자 ID
     * @param request 가입 요청
     * @return 가입한 가족 그룹 정보
     */
    @Transactional
    public FamilyResponse joinFamily(Long userId, JoinFamilyRequest request) {
        log.info("Joining family group. userId: {}, inviteCode: {}", userId, request.inviteCode());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + userId));

        Family family = familyRepository.findByInviteCode(request.inviteCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다. inviteCode: " + request.inviteCode()));

        // 이미 가입되어 있는지 확인
        if (familyMemberRepository.existsByUserAndFamily(user, family)) {
            throw new IllegalStateException("이미 해당 가족 그룹에 가입되어 있습니다.");
        }

        // 가족 구성원으로 추가
        FamilyMember member = FamilyMember.builder()
                .user(user)
                .family(family)
                .role(request.role())
                .nickname(request.nickname())
                .build();

        family.addMember(member);
        familyMemberRepository.save(member);

        log.info("Successfully joined family group. familyId: {}, userId: {}", family.getId(), userId);

        return FamilyResponse.from(family);
    }

    /**
     * 내가 속한 모든 가족 그룹 조회
     * @param userId 사용자 ID
     * @return 가족 그룹 목록
     */
    public List<FamilyResponse> getMyFamilies(Long userId) {
        log.info("Getting user's families. userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + userId));

        List<FamilyMember> members = familyMemberRepository.findByUser(user);

        return members.stream()
                .map(FamilyMember::getFamily)
                .map(FamilyResponse::from)
                .toList();
    }

    /**
     * 특정 가족 그룹 조회
     * @param familyId 가족 그룹 ID
     * @param userId 조회하는 사용자 ID (권한 확인용)
     * @return 가족 그룹 정보
     */
    public FamilyResponse getFamily(Long familyId, Long userId) {
        log.info("Getting family group. familyId: {}, userId: {}", familyId, userId);

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다. familyId: " + familyId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + userId));

        // 해당 가족 그룹에 속해있는지 확인
        if (!familyMemberRepository.existsByUserAndFamily(user, family)) {
            throw new IllegalStateException("해당 가족 그룹에 접근 권한이 없습니다.");
        }

        return FamilyResponse.from(family);
    }

    /**
     * 가족 그룹에서 탈퇴
     * @param familyId 가족 그룹 ID
     * @param userId 탈퇴하는 사용자 ID
     */
    @Transactional
    public void leaveFamily(Long familyId, Long userId) {
        log.info("Leaving family group. familyId: {}, userId: {}", familyId, userId);

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다. familyId: " + familyId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + userId));

        FamilyMember member = familyMemberRepository.findByUserAndFamily(user, family)
                .orElseThrow(() -> new IllegalStateException("해당 가족 그룹에 속해있지 않습니다."));

        familyMemberRepository.delete(member);

        log.info("Successfully left family group. familyId: {}, userId: {}", familyId, userId);
    }
}
