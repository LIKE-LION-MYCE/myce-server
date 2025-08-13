package com.myce.expo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotBlank(message = "행사명을 입력해주세요.")
    private String name;

    @NotNull(message = "행사 날짜를 입력해주세요.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    @NotNull(message = "시작 시간을 입력해주세요.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "종료 시간을 입력해주세요.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotBlank(message = "장소를 입력해주세요.")
    private String location;

    @NotBlank(message = "담당자 이름을 입력해주세요.")
    private String contactName;

    @NotBlank(message = "담당자 연락처를 입력해주세요.")
    private String contactPhone;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "담당자 이메일을 입력해주세요.")
    private String contactEmail;

    @NotBlank(message = "행사 설명을 입력해주세요.")
    private String description;
}
