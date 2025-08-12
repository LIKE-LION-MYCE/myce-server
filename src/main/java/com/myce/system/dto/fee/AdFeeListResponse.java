package com.myce.system.dto.fee;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class AdFeeListResponse {
    private final int currentPage;
    private final int totalPage;
    private final List<AdFeeResponse> adFeeList;

    public AdFeeListResponse(int currentPage, int totalPage) {
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.adFeeList = new ArrayList<>();
    }

    public void addAdFee(AdFeeResponse adFeeResponse) {
        this.adFeeList.add(adFeeResponse);
    }
}
