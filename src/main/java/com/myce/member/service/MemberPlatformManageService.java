package com.myce.member.service;

import com.myce.common.dto.PageResponse;
import com.myce.member.dto.MemberInfoResponse;

public interface MemberPlatformManageService {
    PageResponse<MemberInfoResponse> getMemberList(int page, int pageSize, boolean latestFirst);
    PageResponse<MemberInfoResponse> filterMemberList(int page, int pageSize, String keyword, boolean latestFirst);
}
