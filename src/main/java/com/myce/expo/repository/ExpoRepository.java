package com.myce.expo.repository;

import com.myce.expo.entity.Expo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpoRepository extends JpaRepository<Expo, Long> {

}
