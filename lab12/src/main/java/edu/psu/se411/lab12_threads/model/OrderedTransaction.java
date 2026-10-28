package edu.psu.se411.lab12_threads.model;

/**
 * Resource ordering: the two accounts are always locked lowest id first, whichever
 * way the money is moving.
 *
 * <p>A deadlock needs a cycle in the wait-for graph. If every thread takes locks in
 * one globally agreed order, a cycle cannot form — a thread only ever waits on an
 * account greater than the one it already holds, and "greater than" has no loops.
 */
public class OrderedTransaction extends Transaction {

	public OrderedTransaction(CryptoAccount sender, CryptoAccount receiver, Double amount) {
		super(sender, receiver, amount);
	}

	@Override
	protected CryptoAccount[] lockOrder() {
		CryptoAccount sender = getSender();
		CryptoAccount receiver = getReceiver();

		return sender.compareTo(receiver) <= 0
				? new CryptoAccount[] { sender, receiver }
				: new CryptoAccount[] { receiver, sender };
	}
}
