package com.myce.system.repository;

import com.myce.system.document.EmailLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface EmailLogRepository extends MongoRepository<EmailLog,String> {
    Page<EmailLog> findByExpoId(Long expoId, Pageable pageable);

    @Query("{ 'expoId': ?0, " +
            "$or: [ " +
            "  { 'subject': { $regex: ?1, $options: 'i' } }, " +
            "  { 'content': { $regex: ?1, $options: 'i' } } " +
            "] }")
    Page<EmailLog> searchByExpoIdAndKeyword(Long expoId, String keyword, Pageable pageable);
}