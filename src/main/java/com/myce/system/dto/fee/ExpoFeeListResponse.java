package com.myce.system.dto.fee;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class ExpoFeeListResponse {
    private final int currentPage;
    private final int totalPage;
    private final List<ExpoFeeResponse> expoFeeList;

    public ExpoFeeListResponse(int currentPage, int totalPage) {
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.expoFeeList = new ArrayList<>();
    }

    public void addExpoFee(ExpoFeeResponse expoFeeResponse) {
        this.expoFeeList.add(expoFeeResponse);
    }
}
