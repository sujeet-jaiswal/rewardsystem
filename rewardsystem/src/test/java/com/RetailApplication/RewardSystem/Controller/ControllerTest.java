package com.RetailApplication.RewardSystem.Controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.poc.retailapp.rewardsystem.rewardsystem.controller.Controller;
import com.poc.retailapp.rewardsystem.rewardsystem.dto.BulkSubTransaction;
import com.poc.retailapp.rewardsystem.rewardsystem.dto.BulkTransactionRequest;
import com.poc.retailapp.rewardsystem.rewardsystem.dto.CustomerRewardResponse;
import com.poc.retailapp.rewardsystem.rewardsystem.dto.TransactionRequest;
import com.poc.retailapp.rewardsystem.rewardsystem.service.RewardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Controller.class)
class ControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private RewardService rewardService;

  @Test
  @DisplayName("Test getCustomerRewards success")
  void testGetCustomerRewards() throws Exception {
    UUID customerId = UUID.randomUUID();
    CustomerRewardResponse response = new CustomerRewardResponse();
    response.setTotalPoints(100);

    when(rewardService.getCustomerRewards(eq(customerId), anyInt())).thenReturn(response);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/reward/customer/{customerId}", customerId)
                .param("months", "3"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("{\"totalPoints\":100}"));
  }

  @Test
  @DisplayName("Test getCustomerRewards fails when months is invalid")
  void testGetCustomerRewards_InvalidMonths() throws Exception {
    UUID customerId = UUID.randomUUID();

    // months < 1
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/reward/customer/{customerId}", customerId)
                .param("months", "0"))
        .andExpect(status().isBadRequest());
    // months > 12
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/reward/customer/{customerId}", customerId)
                .param("months", "13"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Test getRewardsAllCustomers success for valid input")
  public void testGetRewardsAllCustomers_ValidMonths() throws Exception {
    // creating mock response
    CustomerRewardResponse customerRewardResponse1 = new CustomerRewardResponse();
    customerRewardResponse1.setTotalPoints(100);
    CustomerRewardResponse customerRewardResponse2 = new CustomerRewardResponse();
    customerRewardResponse2.setTotalPoints(150);
    List<CustomerRewardResponse> response =
        Arrays.asList(customerRewardResponse1, customerRewardResponse2);

    when(rewardService.getAllCustomerRewards(3)).thenReturn(response);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/reward")
                .param("months", "3")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].totalPoints").value(100))
        .andExpect(jsonPath("$[1].totalPoints").value(150));
  }

  @Test
  @DisplayName("Test getRewardsAllCustomers for invalid months less than 1")
  public void testGetRewardsAllCustomers_InvalidMonths_LessThan1() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/reward")
                .param("months", "0")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Test getRewardsAllCustomers for invalid months more than 1")
  public void testGetRewardsAllCustomers_InvalidMonths_GreaterThan12() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/reward")
                .param("months", "13")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Test handleTransaction success")
  void testHandleTransaction() throws Exception {
    UUID customerId = UUID.randomUUID();
    TransactionRequest transactionRequest = new TransactionRequest();
    transactionRequest.setCustomerId(customerId);
    transactionRequest.setAmount(new BigDecimal("120"));
    doNothing()
        .when(rewardService)
        .handleTransaction(
            transactionRequest.getCustomerId(),
            transactionRequest.getAmount(),
            LocalDateTime.now());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/transaction/createTransaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":\"" + customerId + "\", \"amount\":120}"))
        .andExpect(status().isOk())
        .andExpect(content().string("Transaction processed and rewards awarded"));
  }

  @Test
  @DisplayName("Test handleTransaction failure for invalid request")
  void testHandleTransaction_InvalidRequest() throws Exception {
    // CustomerID is not present
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/transaction/createTransaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":120}"))
        .andExpect(status().isBadRequest());

    // Amount is not present
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/transaction/createTransaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":\"" + UUID.randomUUID() + "\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Test handleBulkTransaction success")
  void testHandleBulkTransaction() throws Exception {
    UUID customerId = UUID.randomUUID();
    BulkTransactionRequest bulkTransactionRequest = new BulkTransactionRequest();
    bulkTransactionRequest.setCustomerId(customerId);
    bulkTransactionRequest.setTransactions(
        Arrays.asList(
            new BulkSubTransaction(new BigDecimal("120"), LocalDate.now()),
            new BulkSubTransaction(new BigDecimal("80"), LocalDate.now().minusDays(1))));

    when(rewardService.handleBulkTransactions(eq(customerId), any()))
        .thenReturn(bulkTransactionRequest);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/transaction/createBulkTransaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"customerId\":\""
                        + customerId
                        + "\", \"transactions\":[{\"amount\":120, \"transactionDate\":\""
                        + LocalDate.now()
                        + "\"}, {\"amount\":80, \"transactionDate\":\""
                        + LocalDate.now().minusDays(1)
                        + "\"}]}"))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .json(
                    "{\"customerId\":\""
                        + customerId
                        + "\", \"transactions\":[{\"amount\":120, \"transactionDate\":\""
                        + LocalDate.now()
                        + "\"}, {\"amount\":80, \"transactionDate\":\""
                        + LocalDate.now().minusDays(1)
                        + "\"}]}"));
  }

  @Test
  @DisplayName("Test handleBulkTransaction fails for invalid request body")
  void testHandleBulkTransaction_InvalidRequest() throws Exception {
    // Request body does not have customerId
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/transaction/createBulkTransaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"transactions\":[{\"amount\":120, \"transactionDate\":\""
                        + LocalDate.now()
                        + "\"}]}"))
        .andExpect(status().isBadRequest());

    // Request body does not have amount
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/transaction/createBulkTransaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":\"" + UUID.randomUUID() + "\"}"))
        .andExpect(status().isBadRequest());
  }
}
