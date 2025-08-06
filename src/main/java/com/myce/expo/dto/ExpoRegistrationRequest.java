package com.myce.expo.dto;

import com.myce.expo.entity.Category;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExpoRegistrationRequest {
  private Long memberId;  // 박람회 등록 아이디 => 로그인 사용자 정보
  private String thumbnailUrl; // 박람회 포스터
  private String title; // 박람회 이름
  private LocalDate startDate;// 박람회 개최 시작일
  private LocalDate endDate; // 박람회 개최 종료일
  private LocalDate displayStartDate;// 박람회 게시 시작일
  private LocalDate displayEndDate; // 박람회 게시 종료일
  private String location; // 박람회 장소
  private String locationDetail;  // 박람회 세부 장소
  private BigDecimal latitude; // 박람회 위도
  private BigDecimal longitude; // 박람회 경도
  private LocalTime startTime; // 박람회 운영 시작시간
  private LocalTime endTime; // 박람회 운영 종료시간
  private Integer maxReserverCount; // 최대 수용 인원
  private String description; // 박람회 상세 소개
  private List<Long> categoryIds; // 박람회 카테고리 id 당은 배열
  private Boolean isPremium; // 프리미엄 상위 노출 서비스 신청 여부

  private ExpoRegistrationCompanyRequest expoRegistrationCompanyRequest;
}
