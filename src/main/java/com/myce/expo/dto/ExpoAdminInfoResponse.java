package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ExpoAdminInfoResponse {
    private String superAdminUsername;
    private String superAdminEmail;
    private String superAdminNickname;

    private List<String> subAdmins;

    @Builder
    public ExpoAdminInfoResponse(String  superAdminUsername, String superAdminEmail,
                                 String superAdminNickname, List<String> subAdmins) {
        this.superAdminUsername = superAdminUsername;
        this.superAdminEmail = superAdminEmail;
        this.superAdminNickname = superAdminNickname;
        this.subAdmins = subAdmins;
    }
}
