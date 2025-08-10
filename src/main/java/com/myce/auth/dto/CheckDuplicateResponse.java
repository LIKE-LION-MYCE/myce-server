package com.myce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckDuplicateResponse {
    private boolean isDuplicate;
}
