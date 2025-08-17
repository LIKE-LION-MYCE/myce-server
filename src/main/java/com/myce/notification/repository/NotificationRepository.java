package com.myce.notification.repository;

import com.myce.notification.document.Notification;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByMemberId(Long memberId, Sort sort);
    
    @Query("{'_id': ?0, 'memberId': ?1}")
    @Update("{'$set': {'isRead': true, 'readAt': ?2}}")
    void markAsRead(String notificationId, Long memberId, LocalDateTime readAt);
    
    @Query("{'memberId': ?0, 'isRead': false}")
    @Update("{'$set': {'isRead': true, 'readAt': ?1}}")
    void markAllAsReadByMemberId(Long memberId, LocalDateTime readAt);
}
