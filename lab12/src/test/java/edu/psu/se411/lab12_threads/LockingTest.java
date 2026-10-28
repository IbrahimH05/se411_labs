package edu.psu.se411.lab12_threads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import edu.psu.se411.lab12_threads.exceptions.InsufficientBalanceException;
import edu.psu.se411.lab12_threads.model.CryptoAccount;
import edu.psu.se411.lab12_threads.model.OrderedTransaction;
import edu.psu.se411.lab12_threads.model.Transaction;

public class LockingTest {

	private static final double TOLERANCE = 1e-6;

	@AfterEach
	public void resetLockWindow() {
		Transaction.setLockWindowNanos(0);
	}

	// Transfers
	@Test
	public void a_transfer_moves_money_and_conserves_the_total() throws Exception {
		CryptoAccount a = new CryptoAccount("acc_1", 1000.0);
		CryptoAccount b = new CryptoAccount("acc_2", 1000.0);

		new OrderedTransaction(a, b, 300.0).execute();

		assertEquals(700.0, a.getBalance(), TOLERANCE);
		assertEquals(1300.0, b.getBalance(), TOLERANCE);
	}

	@Test
	public void an_overdraw_is_refused_and_leaves_both_accounts_untouched() {
		CryptoAccount a = new CryptoAccount("acc_1", 100.0);
		CryptoAccount b = new CryptoAccount("acc_2", 100.0);

		assertThrows(InsufficientBalanceException.class,
				() -> new OrderedTransaction(a, b, 500.0).execute());

		assertEquals(100.0, a.getBalance(), TOLERANCE);
		assertEquals(100.0, b.getBalance(), TOLERANCE);
	}

	@Test
	public void every_lock_is_released_even_when_the_transfer_is_refused() {
		CryptoAccount a = new CryptoAccount("acc_1", 10.0);
		CryptoAccount b = new CryptoAccount("acc_2", 10.0);

		assertThrows(InsufficientBalanceException.class,
				() -> new OrderedTransaction(a, b, 999.0).execute());

		// try/finally around both locks is what makes this hold.
		assertTrue(a.getLock().tryLock(), "sender's lock was not released");
		a.getLock().unlock();
		assertTrue(b.getLock().tryLock(), "receiver's lock was not released");
		b.getLock().unlock();
	}

	// Resource ordering
	@Test
	public void accounts_have_a_total_order() {
		CryptoAccount a = new CryptoAccount("acc_1", 1.0);
		CryptoAccount b = new CryptoAccount("acc_2", 1.0);

		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(a) > 0);
		assertEquals(0, a.compareTo(a));
	}

	@Test
	public void two_accounts_sharing_an_id_still_order_consistently() {
		CryptoAccount a = new CryptoAccount("same", 1.0);
		CryptoAccount b = new CryptoAccount("same", 1.0);

		// Whatever the answer is, it must be stable and antisymmetric, or the
		// ordering is not total and the deadlock fix has a hole in it.
		assertNotEquals(0, a.compareTo(b));
		assertEquals(-Integer.signum(a.compareTo(b)), Integer.signum(b.compareTo(a)));
	}

	/**
	 * The regression test for the deadlock fix. The lock window is widened to the
	 * value that reliably deadlocks NaiveTransaction, and opposing transfers are run
	 * over the same pairs. OrderedTransaction must still finish.
	 */
	@RepeatedTest(3)
	public void ordered_transfers_do_not_deadlock_in_opposing_directions() throws Exception {
		Transaction.setLockWindowNanos(2_000_000L);

		int pairs = 50;
		CountDownLatch done = new CountDownLatch(pairs * 2);
		List<Thread> threads = new ArrayList<>();

		for (int i = 0; i < pairs; i++) {
			CryptoAccount a = new CryptoAccount("acc_a" + i, 1000.0);
			CryptoAccount b = new CryptoAccount("acc_b" + i, 1000.0);

			threads.add(runner(new OrderedTransaction(a, b, 10.0), done));
			threads.add(runner(new OrderedTransaction(b, a, 10.0), done));
		}

		threads.forEach(Thread::start);

		assertTrue(done.await(15, TimeUnit.SECONDS),
				"ordered locking deadlocked, which is the one thing it exists to prevent");
	}

	@RepeatedTest(3)
	public void concurrent_transfers_conserve_the_total() throws Exception {
		int accountCount = 10;
		List<CryptoAccount> accounts = new ArrayList<>();
		for (int i = 0; i < accountCount; i++) {
			accounts.add(new CryptoAccount("acc_" + i, 1000.0));
		}

		var executor = Executors.newFixedThreadPool(8);
		for (int i = 0; i < 20000; i++) {
			CryptoAccount from = accounts.get((int) (Math.random() * accountCount));
			CryptoAccount to = accounts.get((int) (Math.random() * accountCount));
			if (from == to) {
				continue;
			}
			executor.execute(new OrderedTransaction(from, to, Math.random() * 50));
		}
		executor.shutdown();
		assertTrue(executor.awaitTermination(60, TimeUnit.SECONDS));

		assertEquals(accountCount * 1000.0, Simulation.sumTotalMoneyInAccounts(accounts), TOLERANCE);
	}

	private Thread runner(Transaction transaction, CountDownLatch done) {
		Thread thread = new Thread(() -> {
			try {
				transaction.run();
			} finally {
				done.countDown();
			}
		});
		thread.setDaemon(true);
		return thread;
	}
}
