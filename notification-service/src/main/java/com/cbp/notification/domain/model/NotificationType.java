package com.cbp.notification.domain.model;

/**
 * Classifies the business event that triggered a customer notification.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public enum NotificationType {

    /**
     * A money transfer was successfully completed.
     * Both the debit and credit operations were applied to the respective accounts.
     */
    TRANSFER_COMPLETED,

    /**
     * A money transfer could not be completed.
     * No funds were moved; the customer is informed of the reason.
     */
    TRANSFER_FAILED
}
