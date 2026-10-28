package edu.psu.se411.lab11_threads.model;

import java.util.concurrent.atomic.AtomicLong;

import edu.psu.se411.lab11_threads.exceptions.InsufficientBalanceException;

public class Transaction implements Runnable {

	private static final AtomicLong SEQUENCE = new AtomicLong();

	private final String id;
	private final CryptoAccount sender;
	private final CryptoAccount receiver;
	private final Double amount;

	/** The constructor the handout's example calls; the id is generated. */
	public Transaction(CryptoAccount sender, CryptoAccount receiver, Double amount) {
		this("tx_" + SEQUENCE.incrementAndGet(), sender, receiver, amount);
	}

	public Transaction(String id, CryptoAccount sender, CryptoAccount receiver, Double amount) {
		if (id == null || sender == null || receiver == null || amount == null || amount < 0) {
			throw new IllegalArgumentException("Invalid transaction arguments");
		}
		this.id = id;
		this.sender = sender;
		this.receiver = receiver;
		this.amount = amount;
	}

	@Override
	public void run() {
		try {
			execute();
		} catch (InsufficientBalanceException e) {
			// A declined transfer is a normal outcome here, not a failure of the run.
		}
	}

	public void execute() throws InsufficientBalanceException {
		if (sender == receiver) {
			throw new IllegalArgumentException("Sender and receiver cannot be the same account");
		}

		sender.retrieveFromBalance(amount);
		receiver.addToBalance(amount);
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
