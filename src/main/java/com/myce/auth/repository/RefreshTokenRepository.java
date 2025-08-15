package com.myce.auth.repository;

public interface RefreshTokenRepository {
    void save(String loginType, Long memberId, String refreshToken, long limitTime);
    String findByLoginTypeAndMemberId(String loginType, Long memberId);
    void deleteByLoginTypeAndMemberId(String loginType, Long memberId);
}
