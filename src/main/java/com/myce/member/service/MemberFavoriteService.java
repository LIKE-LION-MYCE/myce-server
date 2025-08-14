package com.myce.member.service;

public interface MemberFavoriteService {
  boolean saveFavorite(Long memberId, Long expoId);

  boolean deleteFavorite(Long memberId, Long expoId);
}
