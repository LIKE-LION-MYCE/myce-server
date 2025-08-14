package com.myce.system.repository;

import com.myce.system.dto.email.ExpoAdminEmailResponse;
import com.myce.system.document.EmailLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface EmailLogRepository extends MongoRepository<EmailLog,String> {
    Page<ExpoAdminEmailResponse> findByExpoIdAndSubjectContainingIgnoreCaseOrContentContainingIgnoreCase(
            Long expoId,
            String subject,
            String content,
            Pageable pageable
    );
}