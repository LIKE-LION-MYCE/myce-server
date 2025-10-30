package com.myce.payment.entity.type;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CARD("card"),
    EASY_PAY("trans"),
    TRANSFER("vbank"),
    FOREIGN_PAY("foreign_pay"),
    VIRTUAL_ACCOUNT("virtual_account");

    private final String name;

    PaymentMethod(String name) {
        this.name = name;
    }

    public static PaymentMethod getPaymentMethod(String payMethod) {
        return switch (payMethod) {
            case "card" -> PaymentMethod.CARD;
            case "trans" -> PaymentMethod.TRANSFER;
            case "vbank" -> PaymentMethod.VIRTUAL_ACCOUNT;
            case "samsung", "kakaopay", "naverpay", "payco", "lpay", "ssgpay", "tosspay"
                    -> PaymentMethod.EASY_PAY;
            default -> throw new IllegalArgumentException("Unknown payment method: " + payMethod);
        };
    }
}
