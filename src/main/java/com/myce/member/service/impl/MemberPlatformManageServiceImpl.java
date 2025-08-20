package com.myce.member.service.impl;

import com.myce.common.dto.PageResponse;
import com.myce.member.dto.MemberInfoResponse;
import com.myce.member.entity.Member;
import com.myce.member.mapper.MemberInfoMapper;
import com.myce.member.repository.MemberRepository;
import com.myce.member.service.MemberPlatformManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberPlatformManageServiceImpl implements MemberPlatformManageService {
    private final MemberRepository memberRepository;
    private final MemberInfoMapper memberInfoMapper;

    @Override
    public PageResponse<MemberInfoResponse> getMemberList(int page, int pageSize, boolean latestFirst) {
        log.info("getMemberList page: {}, pageSize: {}, latestFirst : {}", page, pageSize, latestFirst);
        Sort sort = latestFirst ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<Member> members = memberRepository.findAll(pageable);
        return memberInfoMapper.toResponsePage(members);
    }

    @Override
    public PageResponse<MemberInfoResponse> filterMemberList(int page, int pageSize, String keyword, boolean latestFirst) {
        Sort sort = latestFirst ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<Member> members = memberRepository.findAllByKeywordContaining(keyword, pageable);

        return memberInfoMapper.toResponsePage(members);
    }
}
