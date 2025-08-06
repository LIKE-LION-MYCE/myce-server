package com.myce.expo.repository;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.ExpoCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpoCategoryRepository extends JpaRepository<ExpoCategory, Long> {
    // 특정 Expo에 연결된 모든 ExpoCategory 엔티티를 찾습니다.
    List<ExpoCategory> findByExpoId(Long expoId);

    // 특정 Expo에 연결된 모든 ExpoCategory 엔티티를 삭제합니다.
    void deleteAllByExpo(Expo expo);
}
