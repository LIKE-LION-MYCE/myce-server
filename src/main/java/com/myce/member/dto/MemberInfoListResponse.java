package com.myce.member.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class MemberInfoListResponse {
    private final int currentPage;
    private final int totalPage;
    private final List<MemberInfoResponse> memberInfos;

    public MemberInfoListResponse(int currentPage, int totalPage) {
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        memberInfos = new ArrayList<>();
    }

    public void addMemberInfo(MemberInfoResponse memberInfoResponse) {
        memberInfos.add(memberInfoResponse);
    }
}
