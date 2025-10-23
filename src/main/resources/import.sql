INSERT INTO message_template_setting
(template_setting_id, template_code, name, channel_type, content, created_at, updated_at, subject, use_image)
VALUES
    (1, 'EMAIL_VERIFICATION', '이메일 인증 템플릿', 'EMAIL',
     '{ "preheader": "회원가입 인증번호입니다.",
        "emailTitle": "이메일 인증번호 안내",
        "greetingMessage": "안녕하세요.",
        "postMessage": "을 완료하기 위해 아래 인증번호를 입력해주세요.",
        "codeLabel": "인증번호",
        "warningPrefix": "중요:",
        "warningMessage": "이 인증번호는",
        "warningSuffix": "후에 만료됩니다.",
        "securityTitle": "🔒 보안 안내",
        "securityContent": "<p style=''margin:0 0 4px 0;''>• 인증번호를 타인과 공유하지 마세요</p><p style=''margin:0 0 4px 0;''>• 본인이 요청하지 않은 인증이라면 즉시 무시해주세요</p><p style=''margin:0;''>• 문제가 있으시면 고객센터로 문의해주세요</p>"
     }',
     '2025-08-09 12:28:46', '2025-08-18 05:10:12', '[MYCE] 이메일 인증 코드 발송 ', 0),

    (3, 'RESET_PASSWORD', '비밀번호 찾기', 'EMAIL',
     '{ "preheader": "비밀번호 찾기 인증번호입니다.",
        "emailTitle": "이메일 인증번호 안내",
        "greetingMessage": "안녕하세요.<br><strong>회원가입</strong>을 완료하기 위해 아래 인증번호를 입력해주세요.",
        "codeLabel": "인증번호",
        "code": "123456",
        "warningPrefix": "중요:",
        "warningMessage": "이 인증번호는",
        "limitTime": "5",
        "warningSuffix": "후에 만료됩니다.",
        "securityTitle": "🔒 보안 안내",
        "securityContent": "<p style=''margin:0 0 4px 0;''>• 인증번호를 타인과 공유하지 마세요</p><p style=''margin:0 0 4px 0;''>• 본인이 요청하지 않은 인증이라면 즉시 무시해주세요</p><p style=''margin:0;''>• 문제가 있으시면 고객센터로 문의해주세요</p>"
     }',
     '2025-08-10 11:49:15', '2025-08-16 19:10:27', '[Myce] 비밀번호 찾기', 0),

    (4, 'EXPO_REMINDER', '박람회 디데이 알림', 'NOTIFICATION',
     '예약하신 박람회 ''{expoTitle}''가 내일 시작됩니다!',
     '2025-08-13 02:36:48', '2025-08-17 09:58:32', '[MYCE]박람회 디데이 알림', 0),

    (5, 'EVENT_REMINDER', '박람회 행사 알림', 'NOTIFICATION',
     '예약하신 ''{eventName}'' 이벤트가 잠시 후 {startTime}에 시작됩니다.',
     '2025-08-13 09:07:25', '2025-08-13 09:07:25', '[MYCE] 박람회 행사 알림', 0),

    (6, 'QR_ISSUED', 'QR 발급 알립', 'NOTIFICATION',
     'QR코드가 발급되었습니다. {expoTitle} 입장 시 사용하세요!',
     '2025-08-17 03:38:12', '2025-08-17 09:10:50', '[MYCE] QR 발급 알림', 0),

    (7, 'QR_REISSUED', 'QR 재발급 알림', 'NOTIFICATION',
     '요청하신 {expoTitle} 입장 QR코드가 재발급 완료되었습니다.',
     '2025-08-17 03:38:12', '2025-08-17 09:10:50', '[MYCE] QR 재발급 알림', 0),

    (8, 'PAYMENT_COMPLETE', '결제 완료 알림', 'NOTIFICATION',
     '{expoTitle} 예매  결제 {paymentAmount}이 정상 처리 되었습니다.',
     '2025-08-17 10:06:08', '2025-08-17 17:50:07', '[MYCE] 예매 완료 알림', 0),

    (9, 'EXPO_STATUS_CHANGE', '박람회 상태 변경 알림', 'NOTIFICATION',
     '신청하신 {expoTitle}의 상태가 {oldStatus} 에서 {newStatus} 로 변경되었습니다.',
     '2025-08-17 18:51:30', '2025-08-17 19:48:03', '[MYCE] 박람회 상태 변경 알림', 0),

    (10, 'AD_STATUS_CHANGE', '광고 상태 변경 알림', 'NOTIFICATION',
     '신청하신 {adTitle}의 상태가 {oldStatus} 에서 {newStatus} 로 변경되었습니다.',
     '2025-08-17 18:51:30', '2025-08-17 19:48:03', '[MYCE] 광고 상태 변경 알림', 0);

-- expo_fee_setting 초기 데이터
INSERT INTO expo_fee_setting
(daily_usage_fee, deposit, is_active, premium_deposit, settlement_commission, created_at, expo_fee_setting_id, updated_at, name)
VALUES
    (5000, 100000, 0, 200000, 10.00, '2025-08-04 13:51:34', 1, '2025-08-18 19:42:22', '기본 요금제'),

    (6000, 150000, 0, 250000, 12.50, '2025-08-04 13:51:34', 2, '2025-08-04 13:51:34', '기본 요금제'),

    (5500, 120000, 0, 220000, 11.00, '2025-08-04 13:51:34', 3, '2025-08-04 13:51:34', '기본 요금제'),

    (5800, 130000, 0, 230000, 13.75, '2025-08-04 13:51:34', 4, '2025-08-04 13:51:34', '기본 요금제'),

    (10000, 2000000, 0, 1000000, 7.00, '2025-08-12 14:09:27', 9, '2025-08-18 19:34:04', '기본 요금제'),

    (15000, 500000, 1, 100000, 5.00, '2025-08-18 19:42:18', 11, '2025-08-18 19:42:22', '신규요금제');

-- category 초기 데이터
INSERT INTO category
(category_id, created_at, updated_at, category_code, name)
VALUES
    (1, '2025-08-04 13:25:49', '2025-08-04 13:25:49', 'TECH', '기술/IT'),

    (2, '2025-08-04 13:25:49', '2025-08-04 13:25:49', 'FASHION', '패션/뷰티'),

    (3, '2025-08-04 13:25:49', '2025-08-04 13:25:49', 'FOOD', '푸드/음료'),

    (4, '2025-08-04 13:25:49', '2025-08-04 13:25:49', 'CULTURE', '문화/예술');

-- refund_fee_setting 초기 데이터
INSERT INTO refund_fee_setting
(end_day, fee_rate, start_day, refund_fee_setting_id, updated_at, description, standard_type, created_at, standard_day_count, is_active, valid_from, valid_until, name)
VALUES
    (NULL, 10.00, NULL, 6, '2025-08-16 10:02:59', '박람회 관람일 20일 전까지 10% 취소 수수료',
     'BEFORE_EXPO_START', '2025-08-16 10:02:59', 20, 1, '2025-08-16 10:22:27', NULL, '[기본]관람 전 20일'),

    (NULL, 40.00, NULL, 7, '2025-08-18 20:38:24', '박람회 관람일 15일 전까지 40% 취소 수수료',
     'BEFORE_EXPO_START', '2025-08-16 10:02:59', 15, 1, '2025-08-16 10:22:27', NULL, '[기본]관람 전 15일'),

    (NULL, 70.00, NULL, 8, '2025-08-16 10:02:59', '박람회 관람일 7일 전까지 70% 취소 수수료',
     'BEFORE_EXPO_START', '2025-08-16 10:02:59', 7, 1, '2025-08-16 10:22:27', NULL, '[기본]관람 전 7일'),

    (NULL, 100.00, NULL, 9, '2025-08-16 10:02:59', '박람회 관람일 3일 전부터 환불 불가',
     'BEFORE_EXPO_START', '2025-08-16 10:02:59', 3, 1, '2025-08-16 10:22:27', NULL, '[기본]관람 전 3일');

-- ad_fee_setting 초기 데이터
INSERT INTO ad_fee_setting
(fee_per_day, is_active, ad_fee_id, ad_position_id, created_at, updated_at, name)
VALUES
    (10000, 0, 1, 1, '2025-08-04 13:50:05', '2025-08-18 19:25:41', '기본 요금제'),

    (15000, 0, 2, 2, '2025-08-04 13:50:05', '2025-08-12 23:43:57', '기본 요금제'),

    (12000, 1, 3, 3, '2025-08-04 13:50:05', '2025-08-04 13:50:05', '기본 요금제'),

    (9000, 1, 4, 4, '2025-08-04 13:50:05', '2025-08-04 13:50:05', '기본 요금제'),

    (100000, 1, 7, 2, '2025-08-12 16:43:40', '2025-08-18 19:25:33', '기본 요금제'),

    (12333, 0, 8, 2, '2025-08-18 19:23:58', '2025-08-18 19:25:33', '신규설정'),

    (2000000, 1, 9, 1, '2025-08-18 19:25:15', '2025-08-18 19:25:41', '신규설정');


-- ad_position 초기 데이터
INSERT INTO ad_position
(image_height, image_width, is_active, max_count, ad_position_id, created_at, updated_at, name)
VALUES
    (400, 1200, 1, 10, 1, '2025-08-04 13:26:51', '2025-08-13 20:09:50', '메인 배너'),

    (200, 500, 1, 1, 2, '2025-08-04 13:26:51', '2025-08-13 19:09:37', '사이드 배너'),

    (300, 1200, 1, 1, 3, '2025-08-04 13:26:51', '2025-08-13 19:11:02', '팝업 광고'),

    (40, 160, 1, 1, 4, '2025-08-04 13:26:51', '2025-08-04 13:26:51', '푸터 배너');

-- member_grade 초기 데이터
INSERT INTO member_grade
(is_active, mileage_rate, created_at, member_grade_id, updated_at, grade_image_url, description, grade_code, base_amount)
VALUES
    (1, 0.01, '2025-08-04 13:10:35', 1, '2025-08-04 13:10:35', 'BRONZE.png', '초보 찍찍이', 'BRONZE', 0),

    (1, 0.02, '2025-08-04 13:10:35', 2, '2025-08-04 13:10:35', 'SILVER.png', '열정 찍찍이', 'SILVER', 100000),

    (1, 0.03, '2025-08-04 13:10:35', 3, '2025-08-04 13:10:35', 'GOLD.png', '정예 찍찍이', 'GOLD', 300000),

    (1, 0.05, '2025-08-04 13:10:35', 4, '2025-08-04 13:10:35', 'PLATINUM.png', '엘리트 찍찍이', 'PLATINUM', 1000000),

    (1, 0.10, '2025-08-15 09:42:50', 5, '2025-08-15 09:42:50', 'DIAMOND.png', '레전드 찍찍이', 'DIAMOND', 2000000);
