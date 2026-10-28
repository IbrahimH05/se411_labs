package edu.psu.se411.lab12_threads.model;

import java.util.concurrent.locks.ReentrantLock;

import edu.psu.se411.lab12_threads.exceptions.InsufficientBalanceException;

/**
 * Lab 12 replaces {@code synchronized} with an explicit {@link ReentrantLock}.
 *
 * <p>The lock is exposed because a transfer has to hold <em>two</em> accounts at
 * once to be atomic — something {@code synchronized} methods cannot express, since
 * each one releases its monitor before the next is taken.
 */
public class CryptoAccount implements Comparable<CryptoAccount> {

	private final String id;
	private final ReentrantLock lock = new ReentrantLock();

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

	public ReentrantLock getLock() {
		return lock;
	}

	/** The caller must hold this account's lock. */
	public void addToBalance(Double amount) {
		if (amount == null || amount < 0) {
			throw new IllegalArgumentException("Amount added to balance cannot be negative");
		}
		balance += amount;
	}

	/** The caller must hold this account's lock. */
	public void retrieveFromBalance(Double amount) throws InsufficientBalanceException {
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

	public Double getBalance() {
		lock.lock();
		try {
			return balance;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * The total order that resource ordering relies on. Lexicographic is fine: what
	 * matters is that every thread agrees on the same order, not that the order is
	 * numerically sensible. Identity breaks ties so the order is total even if two
	 * accounts were given the same id.
	 */
	@Override
	public int compareTo(CryptoAccount other) {
		int byId = id.compareTo(other.id);
		return byId != 0 ? byId : Integer.compare(System.identityHashCode(this), System.identityHashCode(other));
	}

	@Override
	public String toString() {
		return String.format("%s=%.2f", id, getBalance());
	}
}
