package com.myce.dashboard.util;

import com.myce.dashboard.record.CheckDivideZero;

public class ComparisonUtil {
    //지난 기간 결과값이 0일 경우 -> 비교 백분율을 0으로 고정
    public static CheckDivideZero getCheckDivideZero(Long pastResult, Long currentResult) {
        float compareRatio;
        boolean isTrending;
        if(pastResult == 0) {
            compareRatio = 0;
            isTrending = false;
        }else{
            compareRatio = (float) 100 * (currentResult - pastResult) / pastResult;
            isTrending = compareRatio > 0;
        }
        return new CheckDivideZero(compareRatio, isTrending);
    }
}
