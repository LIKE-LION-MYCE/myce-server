package com.myce.member.service;

import com.myce.member.dto.MemberInfoWithMileageResponse;
import com.myce.member.dto.PasswordChangeRequest;

public interface MemberService {
  
    void withdrawMember(Long memberId);

    void changePassword(Long memberId, PasswordChangeRequest request);

    MemberInfoWithMileageResponse getMyInfo(Long memberId);

    Integer getMyMileage(Long memberId);
}