package com.RetailApplication.RewardSystem.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.poc.retailapp.rewardsystem.rewardsystem.dto.BulkSubTransaction;
import com.poc.retailapp.rewardsystem.rewardsystem.dto.BulkTransactionRequest;
import com.poc.retailapp.rewardsystem.rewardsystem.dto.CustomerRewardResponse;
import com.poc.retailapp.rewardsystem.rewardsystem.entity.Customer;
import com.poc.retailapp.rewardsystem.rewardsystem.entity.Transaction;
import com.poc.retailapp.rewardsystem.rewardsystem.exception.RewardServiceException;
import com.poc.retailapp.rewardsystem.rewardsystem.repository.CustomerRepository;
import com.poc.retailapp.rewardsystem.rewardsystem.repository.TransactionRepository;
import com.poc.retailapp.rewardsystem.rewardsystem.service.RewardService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RewardServiceTest {

    @InjectMocks
    private RewardService rewardService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test calculatePoints for various purchase amounts")
    void testCalculatePoints() {
        // when purchase > 100
        assertEquals(90, rewardService.calculatePoints(new BigDecimal("120")));
        // when purchase = 100
        assertEquals(50, rewardService.calculatePoints(new BigDecimal("100")));
        // when purchase >50 but <100
        assertEquals(20, rewardService.calculatePoints(new BigDecimal("70")));
        // when purchase <= 50
        assertEquals(0, rewardService.calculatePoints(new BigDecimal("40")));
    }

    @Test
    @DisplayName("Test getCustomerRewards success")
    void testGetCustomerRewards() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName("xxx");
        customer.setEmail("xxx@example.com");
        customer.setPhoneNumber("1234567890");

        Transaction transaction1 = new Transaction();
        transaction1.setId(UUID.randomUUID());
        transaction1.setCustomer(customer);
        transaction1.setAmount(new BigDecimal("120"));
        transaction1.setTransactionDate(LocalDateTime.now().minusMonths(1));

        Transaction transaction2 = new Transaction();
        transaction2.setId(UUID.randomUUID());
        transaction2.setCustomer(customer);
        transaction2.setAmount(new BigDecimal("80"));
        transaction2.setTransactionDate(LocalDateTime.now().minusMonths(2));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(transactionRepository.findByCustomerIdAndTransactionDateAfter(eq(customerId), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(transaction1, transaction2));

        CustomerRewardResponse response = rewardService.getCustomerRewards(customerId, 3);

        assertNotNull(response);
        assertEquals(90 + 30, response.getTotalPoints());
        assertEquals(2, response.getRewards().size());
    }

    @Test
    @DisplayName("Test getCustomerRewards failure when customer does not exist")
    void testGetCustomerRewards_InvalidCustomerId() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        RewardServiceException exception = assertThrows(RewardServiceException.class,
                () -> rewardService.getCustomerRewards(customerId, 3));

        assertEquals("Invalid Id. Customer not found", exception.getMessage());
    }

    @Test
    @DisplayName("Test getAllCustomerRewards success")
    public void testGetAllCustomerRewards_ValidMonths() throws RewardServiceException {
        UUID customerId1 = UUID.randomUUID();
        Customer customer1 = new Customer();
        customer1.setId(customerId1);
        customer1.setName("xxx");
        customer1.setEmail("xxx@example.com");
        customer1.setPhoneNumber("1234567890");

        UUID customerId2 = UUID.randomUUID();
        Customer customer2 = new Customer();
        customer2.setId(customerId2);
        customer2.setName("yyy");
        customer2.setEmail("yyy@example.com");
        customer2.setPhoneNumber("1234567890");

        List<Customer> customers = Arrays.asList(customer1, customer2);

        when(customerRepository.findAll()).thenReturn(customers);

        Transaction transaction1 = new Transaction();
        transaction1.setId(UUID.randomUUID());
        transaction1.setCustomer(customer1);
        transaction1.setAmount(new BigDecimal("120"));
        transaction1.setTransactionDate(LocalDateTime.now().minusMonths(1));

        Transaction transaction2 = new Transaction();
        transaction2.setId(UUID.randomUUID());
        transaction2.setCustomer(customer2);
        transaction2.setAmount(new BigDecimal("80"));
        transaction2.setTransactionDate(LocalDateTime.now().minusMonths(2));

        when(customerRepository.findById(customerId1)).thenReturn(Optional.of(customer1));
        when(customerRepository.findById(customerId2)).thenReturn(Optional.of(customer2));
        when(transactionRepository.findByCustomerIdAndTransactionDateAfter(eq(customerId1), any(LocalDateTime.class)))
                .thenReturn(List.of(transaction1));
        when(transactionRepository.findByCustomerIdAndTransactionDateAfter(eq(customerId2), any(LocalDateTime.class)))
                .thenReturn(List.of(transaction2));

        List<CustomerRewardResponse> result = rewardService.getAllCustomerRewards(3);

        verify(customerRepository, times(1)).findAll();
        verify(transactionRepository, times(1)).findByCustomerIdAndTransactionDateAfter(eq(customerId1), any(LocalDateTime.class));
        verify(transactionRepository, times(1)).findByCustomerIdAndTransactionDateAfter(eq(customerId2), any(LocalDateTime.class));

        assertEquals(2, result.size());
        assertEquals("xxx", result.get(0).getCustomer().getName());
        assertEquals(90, result.get(0).getTotalPoints());
        assertEquals("yyy", result.get(1).getCustomer().getName());
        assertEquals(30, result.get(1).getTotalPoints());
    }

    @Test
    @DisplayName("Test getAllCustomerRewards fails when no customers found")
    public void testGetAllCustomerRewards_NoCustomers() throws RewardServiceException {
        when(customerRepository.findAll()).thenReturn(Arrays.asList());

        List<CustomerRewardResponse> result = rewardService.getAllCustomerRewards(3);

        verify(customerRepository).findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test handleTransaction success")
    void testHandleTransaction() {
        UUID customerId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("120");
        LocalDateTime transactionDate = LocalDateTime.now();

        Customer customer = new Customer();
        customer.setId(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        rewardService.handleTransaction(customerId, amount, transactionDate);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Test handleTransaction when amount is invalid")
    void testHandleTransaction_InvalidAmount() {
        UUID customerId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("-120");
        LocalDateTime transactionDate = LocalDateTime.now();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> rewardService.handleTransaction(customerId, amount, transactionDate));

        assertEquals("Transaction amount must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Test handleBulkTransactions success")
    void testHandleBulkTransactions() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        BulkSubTransaction subTransaction1 = new BulkSubTransaction(new BigDecimal("120"), LocalDateTime.now().minusDays(1).toLocalDate());
        BulkSubTransaction subTransaction2 = new BulkSubTransaction(new BigDecimal("80"), LocalDateTime.now().minusDays(2).toLocalDate());
        List<BulkSubTransaction> transactions = Arrays.asList(subTransaction1, subTransaction2);

        BulkTransactionRequest response = rewardService.handleBulkTransactions(customerId, transactions);

        assertNotNull(response);
        assertEquals(2, response.getTransactions().size());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }
}
