package com.cbp.account.domain.model;

/**
 * Represents the lifecycle state of a bank account.
 *
 * <p>Status transitions follow a strict finite-state machine:
 * <pre>
 *   PENDING_ACTIVATION → ACTIVE → FROZEN → ACTIVE
 *                                        ↘ CLOSED
 * </pre>
 *
 * <p>Operational rules:
 * <ul>
 *   <li>{@link #PENDING_ACTIVATION} — Account has been created but not yet activated.
 *       No transactions are permitted.</li>
 *   <li>{@link #ACTIVE} — Normal operational state. All debit and credit operations
 *       are permitted subject to balance constraints.</li>
 *   <li>{@link #FROZEN} — Account is temporarily suspended (e.g., AML hold, customer
 *       request). Read operations are allowed; write operations are rejected.</li>
 *   <li>{@link #CLOSED} — Terminal state. No further operations are permitted.</li>
 * </ul>
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public enum AccountStatus {

    /**
     * Account has been registered but has not yet completed activation checks.
     * No financial transactions may be performed in this state.
     */
    PENDING_ACTIVATION,

    /**
     * Account is fully operational. Debits and credits are permitted
     * subject to available balance constraints.
     */
    ACTIVE,

    /**
     * Account is temporarily frozen. No debit or credit operations may
     * be performed. The account can be reactivated to {@link #ACTIVE}.
     */
    FROZEN,

    /**
     * Account has been permanently closed. This is a terminal state;
     * no further transitions are possible.
     */
    CLOSED
}
