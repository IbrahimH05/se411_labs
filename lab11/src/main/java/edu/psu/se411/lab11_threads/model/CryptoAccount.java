package edu.psu.se411.lab11_threads.model;

import edu.psu.se411.lab11_threads.exceptions.InsufficientBalanceException;

/**
 * Part 2 solution: both mutators are synchronised, so a balance update is atomic.
 * {@link UnsafeCryptoAccount} drops the keyword to demonstrate part 1.
 */
public class CryptoAccount {

	private final String id;
	private Double balance;

	public CryptoAccount(String id, Double balance) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Account id is required");
		}
		if (balance == null || balance < 0) {
			throw new IllegalArgumentException("Account balance cannot be negative");
		}
		this.id = id;
		this.balance = balance;
	}

	public synchronized void addToBalance(Double amount) {
		if (amount == null || amount < 0) {
			throw new IllegalArgumentException("Amount added to balance cannot be negative");
		}
		balance += amount;
	}

	public synchronized void retrieveFromBalance(Double amount) throws InsufficientBalanceException {
		if (amount == null || amount < 0) {
			throw new IllegalArgumentException("Amount retrieved from balance cannot be negative");
		}
		if (amount > balance) {
			throw new InsufficientBalanceException(id);
		}
		balance -= amount;
	}

	public String getId() {
		return id;
	}

	public synchronized Double getBalance() {
		return balance;
	}

	@Override
	public String toString() {
		return String.format("%s=%.2f", id, getBalance());
	}
}
