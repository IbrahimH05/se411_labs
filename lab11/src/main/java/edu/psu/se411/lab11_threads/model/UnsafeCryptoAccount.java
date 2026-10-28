package edu.psu.se411.lab11_threads.model;

import edu.psu.se411.lab11_threads.exceptions.InsufficientBalanceException;

/**
 * Part 1: the same account with the synchronisation removed, so the double
 * spending problem can be demonstrated rather than described.
 *
 * <p>An override may drop {@code synchronized} because it is not part of the method
 * signature — it is an implementation detail of the parent, and nothing in the type
 * system carries the guarantee down. That is itself the lesson: thread safety is not
 * inherited.
 */
public class UnsafeCryptoAccount extends CryptoAccount {

	private Double balance;

	public UnsafeCryptoAccount(String id, Double balance) {
		super(id, balance);
		this.balance = balance;
	}

	@Override
	public void addToBalance(Double amount) {
		if (amount == null || amount < 0) {
			throw new IllegalArgumentException("Amount added to balance cannot be negative");
		}
		// Read, add, write. Three steps, no lock: another thread can read the same
		// starting value and overwrite the result of this one.
		balance += amount;
	}

	@Override
	public void retrieveFromBalance(Double amount) throws InsufficientBalanceException {
		if (amount == null || amount < 0) {
			throw new IllegalArgumentException("Amount retrieved from balance cannot be negative");
		}
		if (amount > balance) {
			throw new InsufficientBalanceException(getId());
		}
		balance -= amount;
	}

	@Override
	public Double getBalance() {
		return balance;
	}
}
