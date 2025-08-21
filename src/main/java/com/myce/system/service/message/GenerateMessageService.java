package com.myce.system.service.message;

import com.myce.system.dto.message.MessageTemplate;
import com.myce.system.entity.MessageTemplateSetting;
import com.myce.reservation.entity.code.UserType;
import org.thymeleaf.context.Context;

public interface GenerateMessageService {
    MessageTemplate getMessageForVerification(String verificationName, String code, String limitTime);

    MessageTemplate getMessageForResetPassword(String password);

    MessageTemplate getMessageForReservationConfirmation(String name, String expoTitle, 
            String reservationCode, Integer quantity, String paymentAmount, UserType userType);

    String getFullMessage(MessageTemplateSetting messageTemplate, Context context);
}
