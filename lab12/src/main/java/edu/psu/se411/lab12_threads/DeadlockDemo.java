package edu.psu.se411.lab12_threads;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.se411.lab12_threads.model.CryptoAccount;
import edu.psu.se411.lab12_threads.model.NaiveTransaction;
import edu.psu.se411.lab12_threads.model.OrderedTransaction;
import edu.psu.se411.lab12_threads.model.Transaction;

/**
 * Part 2: what goes wrong when synchronized is replaced by two locks, and the
 * resource-ordering fix.
 *
 * Run with: mvn exec:java -Dexec.mainClass=edu.psu.se411.lab12_threads.DeadlockDemo
 */
public class DeadlockDemo {

	private static final Logger logger = LoggerFactory.getLogger(DeadlockDemo.class);

	private static final int PAIRS = 200;
	private static final long WATCHDOG_SECONDS = 5;

	public static void main(String[] args) {
		logger.info("Application is starting...");

		attempt("NaiveTransaction  (sender first, then receiver)", NaiveTransaction::new);
		attempt("OrderedTransaction (lowest account id first)", OrderedTransaction::new);

		logger.info("Application is closing...");
		System.exit(0);
	}

	@FunctionalInterface
	private interface TransactionFactory {
		Transaction create(CryptoAccount sender, CryptoAccount receiver, Double amount);
	}

	private static void attempt(String label, TransactionFactory factory) {
		System.out.println();
		System.out.println("== " + label + " ==");

		List<Thread> threads = new ArrayList<>();
		CountDownLatch ready = new CountDownLatch(PAIRS * 2);
		CountDownLatch go = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(PAIRS * 2);

		for (int i = 0; i < PAIRS; i++) {
			CryptoAccount a = new CryptoAccount("acc_a" + i, 1000.0);
			CryptoAccount b = new CryptoAccount("acc_b" + i, 1000.0);

			// Opposite directions over the same pair: the classic deadlock shape.
			threads.add(worker(factory.create(a, b, 10.0), ready, go, done));
			threads.add(worker(factory.create(b, a, 10.0), ready, go, done));
		}

		// Hold the first lock for a moment so both threads reliably reach the
		// second one while the other still holds it.
		Transaction.setLockWindowNanos(2_000_000L);

		threads.forEach(Thread::start);

		try {
			ready.await();
			go.countDown();     // release them all at once to maximise interleaving

			boolean finished = done.await(WATCHDOG_SECONDS, TimeUnit.SECONDS);

			if (finished) {
				System.out.printf("  all %d transfers completed%n", PAIRS * 2);
				logger.info("{}: completed without deadlock", label);
			} else {
				System.out.printf("  STILL RUNNING after %d seconds - %d transfer(s) stuck%n",
						WATCHDOG_SECONDS, done.getCount());
				reportDeadlockedThreads();
				logger.error("{}: deadlocked, {} transfers never finished", label, done.getCount());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			Transaction.setLockWindowNanos(0);
		}
	}

	private static Thread worker(Transaction transaction,
			CountDownLatch ready, CountDownLatch go, CountDownLatch done) {

		Thread thread = new Thread(() -> {
			ready.countDown();
			try {
				go.await();
				transaction.run();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				done.countDown();
			}
		});
		thread.setDaemon(true);   // so a deadlocked run still lets the JVM exit
		return thread;
	}

	/** The JVM can identify a deadlock itself, which beats guessing from a hang. */
	private static void reportDeadlockedThreads() {
		ThreadMXBean threads = ManagementFactory.getThreadMXBean();
		long[] deadlocked = threads.findDeadlockedThreads();

		if (deadlocked == null) {
			System.out.println("  (the JVM reports no cyclic deadlock - threads are merely slow)");
			return;
		}

		System.out.printf("  DEADLOCK CONFIRMED by ThreadMXBean: %d threads in a lock cycle%n",
				deadlocked.length);

		var info = threads.getThreadInfo(deadlocked, 2);
		for (int i = 0; i < Math.min(2, info.length); i++) {
			System.out.printf("    %s is BLOCKED on %s held by %s%n",
					info[i].getThreadName(), info[i].getLockName(), info[i].getLockOwnerName());
		}
	}
}
