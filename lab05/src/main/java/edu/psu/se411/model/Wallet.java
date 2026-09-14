// Exercise 2
package edu.psu.se411.model;

import java.math.BigDecimal;

import edu.psu.se411.exceptions.InsufficientFundsException;

public class Wallet {

    private final String owner;
    private BigDecimal balance;

    public Wallet(String owner, BigDecimal openingBalance) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("Owner is required");
        }
        if (openingBalance == null || openingBalance.signum() < 0) {
            throw new IllegalArgumentException("Opening balance cannot be negative");
        }
        this.owner = owner;
        this.balance = openingBalance;
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        balance = balance.add(amount);
    }

    public void withdrawToBank(BigDecimal amount, BankAccount destination)
            throws InsufficientFundsException {

        if (destination == null) {
            throw new IllegalArgumentException("Destination account is required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (amount.compareTo(balance) > 0) {
            throw new InsufficientFundsException(amount, balance);
        }

        balance = balance.subtract(amount);
        destination.deposit(amount);
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
