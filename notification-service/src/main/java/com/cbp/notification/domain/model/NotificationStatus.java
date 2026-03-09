package com.cbp.notification.domain.model;

/**
 * Represents the delivery status of a customer notification.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public enum NotificationStatus {

    /**
     * Notification was successfully dispatched to the customer
     * (email sent or SMS delivered to the provider).
     */
    SENT,

    /**
     * Notification dispatch failed due to a provider error or invalid contact.
     */
    FAILED
}
