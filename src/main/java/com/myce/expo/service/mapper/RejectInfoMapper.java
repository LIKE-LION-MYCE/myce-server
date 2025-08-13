package com.myce.expo.service.mapper;

import com.myce.common.entity.RejectInfo;
import com.myce.common.entity.type.TargetType;

/**
 * RejectInfo 매퍼 클래스
 * Entity 생성을 위한 정적 메서드 제공
 */
public class RejectInfoMapper {

    /**
     * 박람회 거절 시 RejectInfo 엔티티 생성
     */
    public static RejectInfo toEntity(Long expoId, String reason) {
        return RejectInfo.builder()
                .targetType(TargetType.EXPO)
                .targetId(expoId)
                .description(reason)
                .build();
    }
}