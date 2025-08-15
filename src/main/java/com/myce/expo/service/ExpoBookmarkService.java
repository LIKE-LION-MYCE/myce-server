package com.myce.expo.service;

public interface ExpoBookmarkService {
  // 회원이 찜했는지 조회
  boolean getCheckBookmark(Long memberId, Long expoId);
}
