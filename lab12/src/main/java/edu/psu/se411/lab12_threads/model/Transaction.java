package edu.psu.se411.lab12_threads.model;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import edu.psu.se411.lab12_threads.exceptions.InsufficientBalanceException;

/**
 * A transfer that holds both accounts' locks for the whole operation, so no other
 * thread can observe a half-completed transfer.
 *
 * <p>Holding two locks is what makes the transfer atomic and what makes deadlock
 * possible. The order they are taken in is left to the subclass — that single
 * decision is the difference between {@link NaiveTransaction} and
 * {@link OrderedTransaction}.
 */
public abstract class Transaction implements Runnable {

	private static final AtomicLong SEQUENCE = new AtomicLong();

	/**
	 * Demonstration aid, zero by default and therefore inert in normal runs.
	 *
	 * <p>A deadlock needs both threads to hold their first lock at the same moment.
	 * That window is naturally a few nanoseconds wide, so the interleaving is real
	 * but rare — which is exactly what makes deadlocks horrible in production and
	 * hard to show in a lab. Widening the window does not create the bug; it only
	 * makes a bug that is already there happen reliably.
	 */
	private static volatile long lockWindowNanos = 0;

	public static void setLockWindowNanos(long nanos) {
		lockWindowNanos = nanos;
	}

	private final String id;
	private final CryptoAccount sender;
	private final CryptoAccount receiver;
	private final Double amount;

	protected Transaction(CryptoAccount sender, CryptoAccount receiver, Double amount) {
		if (sender == null || receiver == null || amount == null || amount < 0) {
			throw new IllegalArgumentException("Invalid transaction arguments");
		}
		this.id = "tx_" + SEQUENCE.incrementAndGet();
		this.sender = sender;
		this.receiver = receiver;
		this.amount = amount;
	}

	/** The order in which the two accounts' locks are taken. */
	protected abstract CryptoAccount[] lockOrder();

	@Override
	public void run() {
		try {
			execute();
		} catch (InsufficientBalanceException e) {
			// A declined transfer is a normal outcome, not a failed run.
		}
	}

	public void execute() throws InsufficientBalanceException {
		if (sender == receiver) {
			throw new IllegalArgumentException("Sender and receiver cannot be the same account");
		}

		CryptoAccount[] order = lockOrder();
		ReentrantLock first = order[0].getLock();
		ReentrantLock second = order[1].getLock();

		first.lock();
		try {
			if (lockWindowNanos > 0) {
				LockSupport.parkNanos(lockWindowNanos);
			}

			second.lock();
			try {
				sender.retrieveFromBalance(amount);
				receiver.addToBalance(amount);
			} finally {
				second.unlock();
			}
		} finally {
			first.unlock();
		}
	}

	public String getId() {
		return id;
	}

	public CryptoAccount getSender() {
		return sender;
	}

	public CryptoAccount getReceiver() {
		return receiver;
	}

	public Double getAmount() {
		return amount;
	}
}
