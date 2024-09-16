package com.poc.retailapp.rewardsystem.rewardsystem.repository;


import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.poc.retailapp.rewardsystem.rewardsystem.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

}