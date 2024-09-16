package com.poc.retailapp.rewardsystem.rewardsystem.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.poc.retailapp.rewardsystem.rewardsystem.exception.RewardServiceException;

@RestControllerAdvice
@Slf4j
public class RestControllerExceptionHandler {


    @ExceptionHandler(value = RewardServiceException.class)
    public ResponseEntity<?> handleRewardServiceException(
            RuntimeException e) {
        log.error("Error occurred: {}", e.getLocalizedMessage(), e);
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(
            RuntimeException e) {
        log.error("Error occurred: {}", e.getLocalizedMessage(), e);
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception e) {
        log.error("Internal Server Error {}", e.getLocalizedMessage(), e);
        return new ResponseEntity<>("System Error. Please contact support", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
