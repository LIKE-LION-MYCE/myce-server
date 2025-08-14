package com.myce.member.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Expo;
import com.myce.expo.repository.ExpoRepository;
import com.myce.member.entity.Favorite;
import com.myce.member.entity.Member;
import com.myce.member.repository.FavoriteRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.member.service.MemberFavoriteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberFavoriteServiceImpl implements MemberFavoriteService {
  private final FavoriteRepository favoriteRepository;
  private final ExpoRepository expoRepository;
  private final MemberRepository memberRepository;

  @Transactional
  @Override
  public boolean saveFavorite(Long memberId, Long expoId) {
    if(favoriteRepository.existsByMember_IdAndExpo_Id(memberId, expoId)) return true;
    Member member =  memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
    Expo expo = expoRepository.findById(expoId)
        .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

    favoriteRepository.save(new Favorite(member, expo));
    return true;
  }

  @Transactional
  @Override
  public boolean deleteFavorite(Long memberId, Long expoId) {
    favoriteRepository.deleteByMember_IdAndExpo_Id(memberId, expoId);
    return false;
  }
}
