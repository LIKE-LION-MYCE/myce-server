package com.myce.system.dto.adposition;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdPositionDropdownResponse {
    private Long id;
    private String name;
    @Builder
    public AdPositionDropdownResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
