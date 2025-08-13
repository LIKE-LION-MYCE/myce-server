package com.myce.system.repository;

import com.myce.system.document.EmailLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmailLogRepository extends MongoRepository<EmailLog,String> {
}
