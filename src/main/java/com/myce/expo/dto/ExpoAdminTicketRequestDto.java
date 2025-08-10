package com.myce.expo.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ExpoAdminTicketRequestDto {
    @NotBlank(message = "티켓 이름은 필수입니다.")
    @Size(max = 100, message = "티켓 이름은 100자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "티켓 설명은 필수입니다.")
    private String description;

    @NotBlank(message = "티켓 타입은 필수입니다.")
    private String type;

    @NotNull(message = "티켓 가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "티켓 수량은 필수입니다.")
    @Min(value = 1, message = "티켓 수량은 1장 이상이어야 합니다.")
    private Integer totalQuantity;

    @NotNull(message = "판매 시작일은 필수입니다.")
    private LocalDate saleStartDate;

    @NotNull(message = "판매 종료일은 필수입니다.")
    private LocalDate saleEndDate;

    @AssertTrue(message = "판매 종료일은 시작일과 같거나 이후여야 합니다.")
    public boolean isSaleEndDateValid(){
        if (saleStartDate == null || saleEndDate == null) return true;
        return !saleEndDate.isBefore(saleStartDate);
    }

    @NotNull(message = "사용 시작일은 필수입니다.")
    private LocalDate useStartDate;

    @NotNull(message = "사용 종료일은 필수입니다.")
    private LocalDate useEndDate;

    @AssertTrue(message = "사용 종료일은 시작일과 같거나 이후여야 합니다.")
    public boolean isUseEndDateValid(){
        if (useStartDate == null || useEndDate == null) return true;
        return !useEndDate.isBefore(useStartDate);
    }

    @AssertTrue(message = "사용 시작일은 판매 시작일과 같거나 이후여야 합니다.")
    public boolean isUseStartAfterSaleStart(){
        if (useStartDate == null || saleStartDate == null) return true;
        return !useStartDate.isBefore(saleStartDate);
    }
}