package com.myce.receipt.document;

import com.myce.receipt.document.code.ReceiptType;
import com.myce.receipt.document.code.TargetType;
import lombok.*;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@NoArgsConstructor
@Document(collection = "receipts")
public class Receipt {

    @Id
    private String id;
    private ReceiptType type;
    private Long memberId;
    private Long targetId;
    private TargetType targetType;
    private Long totalAmount;
    private List<ReceiptItem> items;
    private LocalDateTime createdAt;

    public Receipt(ReceiptType type, Long memberId, Long targetId, TargetType targetType,
                   Long totalAmount, List<ReceiptItem> items) {
        this.type = type;
        this.memberId = memberId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.totalAmount = totalAmount;
        this.items = items;
        this.createdAt = LocalDateTime.now();
    }
}
