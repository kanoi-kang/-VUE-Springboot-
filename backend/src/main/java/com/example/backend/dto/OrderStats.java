package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStats {
    private Long total;
    private Long pendingPayment;
    private Long pendingShipment;
    private Long pendingReceipt;
    private Long completed;
    private Long cancelled;
    private Double todayAmount;
}