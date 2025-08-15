package com.myce.auth.service.impl;

import com.myce.auth.dto.CheckDuplicateResponse;
import com.myce.auth.dto.FindLoginIdResponse;
import com.myce.auth.dto.SignupRequest;
import com.myce.auth.dto.TempPasswordRequest;
import com.myce.auth.service.AuthService;
import com.myce.auth.service.mapper.AuthMapper;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.util.RandomCodeGenerateUtil;
import com.myce.member.entity.Member;
import com.myce.member.entity.MemberGrade;
import com.myce.member.entity.type.GradeCode;
import com.myce.member.repository.MemberGradeRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.notification.service.EmailSendService;
import com.myce.system.dto.message.MessageTemplate;
import com.myce.system.service.message.GenerateMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int TEMP_PASSWORD_LENGTH = 8;

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final MemberGradeRepository memberGradeRepository;
    private final RandomCodeGenerateUtil randomCodeGenerateUtil;
    private final GenerateMessageService messageTemplateService;
    private final EmailSendService emailSendService;

    public void signup(SignupRequest signupRequest) {
        if(memberRepository.existsByLoginId(signupRequest.getLoginId())) {
            throw new CustomException(CustomErrorCode.ALREADY_EXIST_LOGIN_ID);
        }

        if(memberRepository.existsByEmail(signupRequest.getEmail())) {
            throw new CustomException(CustomErrorCode.ALREADY_EXIST_EMAIL);
        }

        MemberGrade memberGrade = memberGradeRepository.findByGradeCode(GradeCode.BRONZE).orElseThrow();
        String password = passwordEncoder.encode(signupRequest.getPassword());
        Member member = authMapper.signupRequestToMember(signupRequest, memberGrade, password);

        memberRepository.save(member);
    }

    @Override
    public FindLoginIdResponse getLoginId(String name, String email) {
        Member member = memberRepository.findByNameAndEmail(name, email)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        return authMapper.getFindLoginIdResponse(member.getLoginId());
    }

    @Override
    @Transactional
    public void sendTempPasswordMail(TempPasswordRequest request) {
        Member member = memberRepository
                .findByNameAndEmailAndLoginId(request.getName(), request.getEmail(), request.getLoginId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        String tempPassword = randomCodeGenerateUtil.generateRandomCode(TEMP_PASSWORD_LENGTH);
        member.resetPassword(passwordEncoder.encode(tempPassword));

        MessageTemplate messageTemplate = messageTemplateService.getMessageForResetPassword(tempPassword);
        emailSendService.sendMail(request.getEmail(), messageTemplate.getSubject(), messageTemplate.getContent());
    }

    @Override
    public CheckDuplicateResponse checkDuplication(String loginId) {
        boolean isExist = memberRepository.existsByLoginId(loginId);
        return authMapper.getDuplicateResponse(isExist);
    }
}
