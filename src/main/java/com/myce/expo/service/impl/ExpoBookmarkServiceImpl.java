package com.myce.expo.service.impl;

import com.myce.expo.service.ExpoBookmarkService;
import com.myce.member.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpoBookmarkServiceImpl implements ExpoBookmarkService {
  private FavoriteRepository favoriteRepository;

  @Override
  public boolean getCheckBookmark(Long memberId, Long expoId) {
    return favoriteRepository.existsByMemberIdAndExpoId(memberId, expoId);
  }
}
