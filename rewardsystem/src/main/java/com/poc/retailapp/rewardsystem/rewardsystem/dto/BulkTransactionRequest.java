package com.poc.retailapp.rewardsystem.rewardsystem.dto;

import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class BulkTransactionRequest {
    private UUID customerId;
    private List<BulkSubTransaction> transactions;
}
