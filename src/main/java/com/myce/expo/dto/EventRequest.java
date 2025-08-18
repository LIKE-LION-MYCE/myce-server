package com.myce.expo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.time.LocalTime;

// 시간 검증을 위한 커스텀 어노테이션
@Constraint(validatedBy = EventRequest.TimeRangeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface ValidTimeRange {
    String message() default "시작시간은 종료시간보다 빨라야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// 시작시간 분 단위 검증을 위한 커스텀 어노테이션
@Constraint(validatedBy = EventRequest.ValidStartTimeMinuteValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface ValidStartTimeMinute {
    String message() default "시작시간은 정각(00분) 또는 30분만 입력 가능합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidTimeRange
public class EventRequest {

    @NotBlank(message = "행사명을 입력해주세요.")
    private String name;

    @NotNull(message = "행사 날짜를 입력해주세요.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    @NotNull(message = "시작 시간을 입력해주세요.")
    @JsonFormat(pattern = "HH:mm")
    @ValidStartTimeMinute
    private LocalTime startTime;

    @NotNull(message = "종료 시간을 입력해주세요.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotBlank(message = "장소를 입력해주세요.")
    private String location;

    @NotBlank(message = "담당자 이름을 입력해주세요.")
    private String contactName;

    @NotBlank(message = "담당자 연락처를 입력해주세요.")
    @Pattern(regexp = "^[0-9-]+$", message = "올바른 전화번호 형식을 입력하세요. (숫자와 하이픈만 허용)")
    private String contactPhone;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "담당자 이메일을 입력해주세요.")
    private String contactEmail;

    @NotBlank(message = "행사 설명을 입력해주세요.")
    private String description;

    // 시간 범위 검증 클래스
    public static class TimeRangeValidator implements ConstraintValidator<ValidTimeRange, EventRequest> {
        @Override
        public boolean isValid(EventRequest eventRequest, ConstraintValidatorContext context) {
            if (eventRequest.getStartTime() == null || eventRequest.getEndTime() == null) {
                return true; // null 체크는 @NotNull에서 처리
            }
            return eventRequest.getStartTime().isBefore(eventRequest.getEndTime());
        }
    }

    // 시작시간 분 단위 검증 클래스
    public static class ValidStartTimeMinuteValidator implements ConstraintValidator<ValidStartTimeMinute, LocalTime> {
        @Override
        public boolean isValid(LocalTime startTime, ConstraintValidatorContext context) {
            if (startTime == null) {
                return true; // null 체크는 @NotNull에서 처리
            }
            // 분이 0 또는 30인 경우만 허용
            int minute = startTime.getMinute();
            return minute == 0 || minute == 30;
        }
    }
}
