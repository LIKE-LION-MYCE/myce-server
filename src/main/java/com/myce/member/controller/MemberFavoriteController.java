package com.myce.member.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.member.service.MemberFavoriteService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class MemberFavoriteController {
  private final MemberFavoriteService memberFavoriteService;

  // 북마크 등록
  @PostMapping("/{expoId}")
  public ResponseEntity<Map<String, Boolean>> saveFavorite(
      @AuthenticationPrincipal CustomUserDetails member, @PathVariable Long expoId){
    boolean isBookmark = memberFavoriteService.saveFavorite(member.getMemberId(),  expoId);
    return ResponseEntity.ok(Map.of("isBookmark", isBookmark));
  }

  // 북마크 취소
  @DeleteMapping("/{expoId}")
  public ResponseEntity<Map<String, Boolean>> deleteFavorite(
      @AuthenticationPrincipal CustomUserDetails member, @PathVariable Long expoId){
    boolean isBookmark = memberFavoriteService.deleteFavorite(member.getMemberId(),  expoId);
    return ResponseEntity.ok(Map.of("isBookmark", isBookmark));
  }
}
