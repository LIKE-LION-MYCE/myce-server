package com.myce.system.dto.email;

import com.myce.system.document.EmailLog;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ExpoAdminEmailRequest {

    private boolean selectAllMatching = false;

    @Valid
    private List<EmailLog.RecipientInfo> recipientInfos;

    @NotBlank(message = "제목 입력은 필수입니다.")
    private String subject;

    @NotBlank(message = "내용 입력은 필수입니다.")
    private String content;

    @AssertTrue(message = "수신자는 비어있을 수 없습니다.")
    public boolean validateRecipientInfos() {
        if (selectAllMatching) return true;
        return recipientInfos != null && !recipientInfos.isEmpty();
    }
}