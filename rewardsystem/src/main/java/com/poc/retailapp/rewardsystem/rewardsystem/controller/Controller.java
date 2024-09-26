package com.poc.retailapp.rewardsystem.rewardsystem.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.poc.retailapp.rewardsystem.rewardsystem.dto.BulkTransactionRequest;
import com.poc.retailapp.rewardsystem.rewardsystem.dto.CustomerRewardResponse;
import com.poc.retailapp.rewardsystem.rewardsystem.dto.TransactionRequest;
import com.poc.retailapp.rewardsystem.rewardsystem.exception.RewardServiceException;
import com.poc.retailapp.rewardsystem.rewardsystem.service.RewardService;

@RestController
@RequestMapping("/api")
public class Controller {
  @Autowired private RewardService rewardService;



  /**
   * Get rewards for a customer
   * @param customerId
   * @param months
   * @return
   */
  @GetMapping("reward/customer/{customerId}")
  public ResponseEntity<CustomerRewardResponse> getCustomerRewards(
      @PathVariable UUID customerId,
      @RequestParam(value = "months", defaultValue = "3") int months) {
    if (months < 1 || months > 12) {
      try {
        throw new RewardServiceException("Invalid months. Months must be between 1 and 12.");
      } catch (RewardServiceException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    CustomerRewardResponse response = rewardService.getCustomerRewards(customerId, months);
    return ResponseEntity.ok(response);
  }

  

  /**
   * Process a single transaction
   * @param transactionRequest
   * @return
   * @throws RewardServiceException 
   */
  @PostMapping("transaction/createTransaction")
  public ResponseEntity<String> handleTransaction(
      @RequestBody TransactionRequest transactionRequest) throws RewardServiceException {
    if (transactionRequest == null
        || transactionRequest.getAmount() == null
        || transactionRequest.getCustomerId() == null) {
      throw new RewardServiceException("Invalid transaction Request");
    }

    rewardService.handleTransaction(
        transactionRequest.getCustomerId(), transactionRequest.getAmount(), LocalDateTime.now());
    return ResponseEntity.ok("Transaction processed and rewards awarded");
  }

  /**
   * Process bulk transactions for a customer
   * @param bulkTransactionRequest
   * @return
   * @throws RewardServiceException 
   */
  @PostMapping("transaction/createBulkTransaction")
  public ResponseEntity<BulkTransactionRequest> handleBulkTransaction(
      @RequestBody BulkTransactionRequest bulkTransactionRequest) throws RewardServiceException {
    if (bulkTransactionRequest.getCustomerId() == null
        || bulkTransactionRequest.getTransactions().isEmpty()) {
      throw new RewardServiceException("Invalid transaction Request");
    }
    return ResponseEntity.ok(
        rewardService.handleBulkTransactions(
            bulkTransactionRequest.getCustomerId(), bulkTransactionRequest.getTransactions()));
  }


}
