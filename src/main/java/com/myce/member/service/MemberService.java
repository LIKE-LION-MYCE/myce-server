package com.myce.member.service;

import com.myce.member.dto.MemberInfoListResponse;
import com.myce.member.dto.MemberInfoResponse;
import com.myce.member.dto.MemberInfoWithMileageResponse;
import com.myce.member.dto.PasswordChangeRequest;
import com.myce.member.entity.type.Role;

public interface MemberService {
  
    void withdrawMember(Long memberId);

    void changePassword(Long memberId, PasswordChangeRequest request);

    MemberInfoListResponse getMemberInfoByRole(int page, String roleKeyword);

    MemberInfoWithMileageResponse getMyInfo(Long memberId);

    Integer getMyMileage(Long memberId);
}