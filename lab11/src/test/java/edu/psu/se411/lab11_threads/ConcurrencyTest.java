package edu.psu.se411.lab11_threads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import edu.psu.se411.lab11_threads.exceptions.InsufficientBalanceException;
import edu.psu.se411.lab11_threads.model.CryptoAccount;
import edu.psu.se411.lab11_threads.model.Transaction;
import edu.psu.se411.lab11_threads.model.UnsafeCryptoAccount;

public class ConcurrencyTest {

	private static final double TOLERANCE = 1e-6;

	// Single-threaded behaviour
	@Test
	public void money_moves_from_sender_to_receiver() throws Exception {
		CryptoAccount a = new CryptoAccount("acc_a", 1000.0);
		CryptoAccount b = new CryptoAccount("acc_b", 1000.0);

		new Transaction(a, b, 250.0).execute();

		assertEquals(750.0, a.getBalance(), TOLERANCE);
		assertEquals(1250.0, b.getBalance(), TOLERANCE);
	}

	@Test
	public void an_overdraw_is_refused_and_changes_nothing() {
		CryptoAccount a = new CryptoAccount("acc_a", 100.0);
		CryptoAccount b = new CryptoAccount("acc_b", 100.0);

		assertThrows(InsufficientBalanceException.class, () -> new Transaction(a, b, 500.0).execute());

		assertEquals(100.0, a.getBalance(), TOLERANCE);
		assertEquals(100.0, b.getBalance(), TOLERANCE);
	}

	@Test
	public void a_transfer_to_the_same_account_is_rejected() {
		CryptoAccount a = new CryptoAccount("acc_a", 100.0);
		assertThrows(IllegalArgumentException.class, () -> new Transaction(a, a, 10.0).execute());
	}

	@Test
	public void the_handout_constructor_generates_an_id() {
		CryptoAccount a = new CryptoAccount("acc_a", 100.0);
		CryptoAccount b = new CryptoAccount("acc_b", 100.0);

		Transaction first = new Transaction(a, b, 1.0);
		Transaction second = new Transaction(a, b, 1.0);

		assertTrue(first.getId().startsWith("tx_"));
		assertTrue(!first.getId().equals(second.getId()), "ids must be unique");
	}

	@Test
	public void negative_amounts_and_balances_are_rejected() {
		assertThrows(IllegalArgumentException.class, () -> new CryptoAccount("acc", -1.0));
		assertThrows(IllegalArgumentException.class, () -> new CryptoAccount("acc", 10.0).addToBalance(-1.0));
		CryptoAccount a = new CryptoAccount("acc_a", 10.0);
		CryptoAccount b = new CryptoAccount("acc_b", 10.0);
		assertThrows(IllegalArgumentException.class, () -> new Transaction(a, b, -5.0));
	}

	// The point of the lab
	@RepeatedTest(5)
	public void the_synchronized_account_conserves_money_under_concurrency() throws Exception {
		double total = runConcurrentTransfers(CryptoAccount::new);
		assertEquals(10_000.0, total, TOLERANCE,
				"a synchronized account must neither invent nor destroy riyals");
	}

	/**
	 * Deliberately not an assertion that the race happens. A test demanding that
	 * unsynchronised code misbehaves is flaky by construction — the interleaving
	 * that loses money is likely, never guaranteed. What is asserted is the part
	 * that must always hold: the unsafe account still runs to completion, and the
	 * safe one above is the only one that can be relied on for the total.
	 */
	@Test
	public void the_unsynchronized_account_still_completes() throws Exception {
		double total = runConcurrentTransfers(UnsafeCryptoAccount::new);
		assertTrue(total > 0, "the run should finish and leave money in the system");
	}

	private double runConcurrentTransfers(BiFunction<String, Double, CryptoAccount> factory)
			throws InterruptedException {

		List<CryptoAccount> accounts = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			accounts.add(factory.apply("acc_" + i, 1000.0));
		}

		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < 2000; i++) {
			CryptoAccount from = accounts.get(i % 10);
			CryptoAccount to = accounts.get((i + 1) % 10);

			Thread thread = new Thread(new Transaction(from, to, 1.0));
			threads.add(thread);
			thread.start();
		}

		for (Thread thread : threads) {
			thread.join();
		}

		double total = 0;
		for (CryptoAccount account : accounts) {
			total += account.getBalance();
		}
		return total;
	}
}
