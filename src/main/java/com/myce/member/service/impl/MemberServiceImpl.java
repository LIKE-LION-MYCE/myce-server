package com.myce.member.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.member.dto.MemberInfoListResponse;
import com.myce.member.dto.MemberInfoWithMileageResponse;
import com.myce.member.dto.PasswordChangeRequest;
import com.myce.member.entity.Member;
import com.myce.member.entity.type.Role;
import com.myce.member.mapper.MemberInfoMapper;
import com.myce.member.repository.MemberRepository;
import com.myce.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberInfoMapper memberInfoMapper;

    @Override
    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = findMemberById(memberId);
        member.withdraw();
    }

    @Override
    @Transactional
    public void changePassword(Long memberId, PasswordChangeRequest request) {
        String newPassword = request.getNewPassword();
        if(!newPassword.equals(request.getConfirmPassword())) {
            throw new CustomException(CustomErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        Member member = findMemberById(memberId);

        String inputPassword = request.getCurrentPassword();
        if(!passwordEncoder.matches(inputPassword, member.getPassword())) {
            throw new CustomException(CustomErrorCode.CURRENT_PASSWORD_NOT_MATCH);
        }

        member.resetPassword(passwordEncoder.encode(newPassword));
        log.debug("[Member] Change password of member. memberId={}", memberId);
    }

    @Override
    public MemberInfoListResponse getMemberInfoByRole(int page, String roleKeyword) {
        Role role = Role.fromName(roleKeyword);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, 15, sort);
        Page<Member> memberPage = memberRepository.findByRole(role, pageable);
        return memberInfoMapper.toListResponseDto(memberPage);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
    }

    @Override
    public MemberInfoWithMileageResponse getMyInfo(Long memberId) {
        Member member = findMemberById(memberId);
        return memberInfoMapper.toResponseDtoWithMileage(member, member.getMemberGrade().getMileageRate());
    }

    @Override
    public Integer getMyMileage(Long memberId) {
        Member member = findMemberById(memberId);
        return member.getMileage();
    }
}