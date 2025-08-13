package com.myce.system.controller;

import com.myce.schedule.jobs.ExpoNotificationScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 박람회 알림 임시 테스트용
// 박람회 알림은 특정 시각에만 가도록 설정되어 있기에, 강제로 지금 알림 보내도록 하는 컨트롤러
@Slf4j
@RestController
@RequestMapping("/api/test/notifications")
@RequiredArgsConstructor
public class TempNotificationTestController {

    private final ExpoNotificationScheduler expoNotificationScheduler;

    @PostMapping("/expo/{expoId}")
    public ResponseEntity<String> sendExpoNotification(@PathVariable Long expoId) {
        log.info("{}", expoId);
        expoNotificationScheduler.testSendNotificationForExpo(expoId);
        return ResponseEntity.ok("[ 박람회 알림 테스트 ] 박람회 ID => " + expoId);
    }
}
