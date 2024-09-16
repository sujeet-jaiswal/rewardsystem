package com.poc.retailapp.rewardsystem.rewardsystem.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poc.retailapp.rewardsystem.rewardsystem.dto.BulkSubTransaction;
import com.poc.retailapp.rewardsystem.rewardsystem.dto.BulkTransactionRequest;
import com.poc.retailapp.rewardsystem.rewardsystem.dto.CustomerResponse;
import com.poc.retailapp.rewardsystem.rewardsystem.dto.CustomerRewardResponse;
import com.poc.retailapp.rewardsystem.rewardsystem.dto.RewardResponse;
import com.poc.retailapp.rewardsystem.rewardsystem.entity.Customer;
import com.poc.retailapp.rewardsystem.rewardsystem.entity.Transaction;
import com.poc.retailapp.rewardsystem.rewardsystem.exception.RewardServiceException;
import com.poc.retailapp.rewardsystem.rewardsystem.repository.CustomerRepository;
import com.poc.retailapp.rewardsystem.rewardsystem.repository.TransactionRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RewardService {

  @Autowired private TransactionRepository transactionRepository;

  @Autowired private CustomerRepository customerRepository;

  /**
   * Calculate reward points based on purchase amount
   * @param amount
   * @return
   */
  public int calculatePoints(BigDecimal amount) {
    int points = 0;
    int amountInt = amount.intValue(); // Discard the decimal part

    if (amountInt > 100) {
      points += (amountInt - 100) * 2;
      amountInt = 100;
    }
    if (amountInt > 50) {
      points += (amountInt - 50);
    }
    return points;
  }

  /**
   * Calculate rewards and prepare report for a customer for given number of months
   * @param customerId
   * @param months
   * @return
 * @throws RewardServiceException 
   */
  public CustomerRewardResponse getCustomerRewards(UUID customerId, int months)  {
    Customer customer=null;
    try {
        customer = customerRepository
            .findById(customerId)
            .orElseThrow(() -> new RewardServiceException("Invalid Id. Customer not found"));
    } catch (RewardServiceException e) {
      
        e.printStackTrace();
    }
   
    LocalDateTime monthsAgo = LocalDateTime.now().minusMonths(months);
    List<Transaction> transactions =
        transactionRepository.findByCustomerIdAndTransactionDateAfter(customerId, monthsAgo);

    int totalPoints =
        transactions.stream()
            .mapToInt(transaction -> calculatePoints(transaction.getAmount()))
            .sum();

    CustomerResponse customerResponse = new CustomerResponse();
    customerResponse.setId(customer.getId());
    customerResponse.setName(customer.getName());
    customerResponse.setEmail(customer.getEmail());
    customerResponse.setPhoneNumber(customer.getPhoneNumber());

    List<RewardResponse> rewardResponses =
        transactions.stream()
            .map(
                transaction -> {
                  RewardResponse rewardResponse = new RewardResponse();
                  rewardResponse.setTransactionId(transaction.getId());
                  rewardResponse.setTransactionAmount(transaction.getAmount());
                  rewardResponse.setPoints(calculatePoints(transaction.getAmount()));
                  rewardResponse.setAwardedDate(transaction.getTransactionDate());
                  return rewardResponse;
                })
            .toList();

    CustomerRewardResponse response = new CustomerRewardResponse();
    response.setCustomer(customerResponse);
    response.setTotalPoints(totalPoints);
    response.setRewards(rewardResponses);
   
    return response;
  }

  /**
   * Calculate rewards for all customers
   * @param months
   * @return
   */
  public List<CustomerRewardResponse> getAllCustomerRewards(int months) throws RewardServiceException
   {
    List<Customer> customers = customerRepository.findAll();
   
    return customers.stream()
            .map(customer -> getCustomerRewards(customer.getId(), months))
            .toList();
  }

  /**
   * Handle and process single transaction for a customer Id
   * @param customerId
   * @param amount
   * @param transactionDate
   */
  @Transactional
  public void handleTransaction(UUID customerId, BigDecimal amount, LocalDateTime transactionDate) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Transaction amount must be greater than zero");
    }

    Customer customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));


    Transaction transaction = new Transaction();
    transaction.setId(UUID.randomUUID());
    transaction.setCustomer(customer);
    transaction.setAmount(amount);
    transaction.setTransactionDate(transactionDate);

    // Save the transaction
    transactionRepository.save(transaction);
  }

  /**
   * Handle single transactiom when customer details already provided
   * @param customer
   * @param amount
   * @param transactionDate
   */
  public void handleTransaction(
      Customer customer, BigDecimal amount, LocalDateTime transactionDate) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Transaction amount must be greater than zero");
    }
   

    Transaction transaction = new Transaction();
    transaction.setId(UUID.randomUUID());
    transaction.setCustomer(customer);
    transaction.setAmount(amount);
    transaction.setTransactionDate(transactionDate);

    // Save the transaction
    transactionRepository.save(transaction);
  }

  /**
   * Process multiple transactions for a customer
   * @param customerId
   * @param transactionRequests
   * @return
   */
  public BulkTransactionRequest handleBulkTransactions(
      UUID customerId, List<BulkSubTransaction> transactionRequests) {
    Customer customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    BulkTransactionRequest response = new BulkTransactionRequest();
    response.setCustomerId(customerId);
    List<BulkSubTransaction> responseSubTransactions = new ArrayList<>();
    transactionRequests.forEach(
        transactionRequest -> {
          try {
            handleTransaction(
                customer,
                transactionRequest.getAmount(),
                transactionRequest.getTransactionDate().atTime(LocalTime.of(10, 0)));
            responseSubTransactions.add(transactionRequest);
          } catch (Exception e) {
           
          }
        });
    response.setTransactions(responseSubTransactions);
    return response;
  }
}