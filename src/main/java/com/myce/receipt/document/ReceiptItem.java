package com.myce.receipt.document;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptItem {
    private String name;
    private Long price;
}
