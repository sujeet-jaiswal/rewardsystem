package com.poc.retailapp.rewardsystem.rewardsystem.exception;


public class RewardServiceException extends Exception {
    public RewardServiceException() {}

    /** @param message */
    public RewardServiceException(String message) {
        super(message);
    }

    /** @param cause */
    public RewardServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public RewardServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public RewardServiceException(
            String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}