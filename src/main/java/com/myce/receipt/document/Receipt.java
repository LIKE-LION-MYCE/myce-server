package com.myce.receipt.document;

import com.myce.receipt.document.code.ReceiptType;
import com.myce.receipt.document.code.TargetType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "receipt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt {

    @Id
    private String id;

    private ReceiptType type; // 결제 or 정산

    private Long memberId;

    private Long targetId;

    private TargetType targetType;

    private Long totalAmount;

    private List<ReceiptItem> items;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
