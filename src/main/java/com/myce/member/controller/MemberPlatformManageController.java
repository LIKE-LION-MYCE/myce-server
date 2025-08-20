package com.myce.member.controller;

import com.myce.common.dto.PageResponse;
import com.myce.member.dto.MemberInfoResponse;
import com.myce.member.service.MemberPlatformManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/platform/admin/member")
public class MemberPlatformManageController {
    private final MemberPlatformManageService memberService;
    private final Integer PAGE_SIZE = 15;

    @GetMapping
    public PageResponse<MemberInfoResponse> getMemberInfos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "false") boolean latestFirst) {
        return memberService.getMemberList(page, PAGE_SIZE, latestFirst);
    }

    @GetMapping("/filter")
    public PageResponse<MemberInfoResponse> filterMemberInfos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean latestFirst
    ){
        return memberService.filterMemberList(page, PAGE_SIZE, keyword, latestFirst);
    }
}
