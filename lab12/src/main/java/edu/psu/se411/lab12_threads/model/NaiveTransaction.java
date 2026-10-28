package edu.psu.se411.lab12_threads.model;

/**
 * "What could go wrong?" — this.
 *
 * <p>Locks the sender first, then the receiver. Two concurrent transfers in opposite
 * directions between the same pair each take one lock and then wait forever for the
 * other. Correct in isolation, deadlocked in company.
 */
public class NaiveTransaction extends Transaction {

	public NaiveTransaction(CryptoAccount sender, CryptoAccount receiver, Double amount) {
		super(sender, receiver, amount);
	}

	@Override
	protected CryptoAccount[] lockOrder() {
		return new CryptoAccount[] { getSender(), getReceiver() };
	}
}
