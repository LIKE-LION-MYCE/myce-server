package com.myce.receipt.document;

import lombok.*;

@Getter
@NoArgsConstructor
public class ReceiptItem {

    private String name;
    private Long price;

    public ReceiptItem(String name, Long price) {
        this.name = name;
        this.price = price;
    }
}
