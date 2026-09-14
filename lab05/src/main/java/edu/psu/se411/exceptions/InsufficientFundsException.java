// Exercise 2
package edu.psu.se411.exceptions;

import java.math.BigDecimal;

public class InsufficientFundsException extends Exception {

    private static final long serialVersionUID = 1L;

    private final BigDecimal requested;
    private final BigDecimal available;

    public InsufficientFundsException(BigDecimal requested, BigDecimal available) {
        super("Cannot withdraw " + requested + ": wallet balance is only " + available
                + " (short by " + requested.subtract(available) + ").");
        this.requested = requested;
        this.available = available;
    }

    public BigDecimal getRequested() {
        return requested;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public BigDecimal getShortfall() {
        return requested.subtract(available);
    }
}
