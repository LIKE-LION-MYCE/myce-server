package com.myce.member.repository;

import com.myce.member.entity.Guest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest,Long> {
  Optional<Guest> findByEmail(String email);

  // 주어진 목록 안ㅇ
  List<Guest> findByEmailIn(List<String> emails);
}
