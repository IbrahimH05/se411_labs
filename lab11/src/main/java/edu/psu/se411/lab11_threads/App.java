package edu.psu.se411.lab11_threads;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.se411.lab11_threads.model.CryptoAccount;
import edu.psu.se411.lab11_threads.model.Transaction;
import edu.psu.se411.lab11_threads.model.UnsafeCryptoAccount;

/**
 * Build and run using Maven goal: clean package exec:java
 */
public class App {

	static Logger logger = LoggerFactory.getLogger(App.class);

	public static Integer nbAccounts = 10;
	public static Double initialBalance = 1000.0;
	public static Integer nbTransactions = 10000;

	/** Separates double's rounding residue from an actually lost transaction. */
	private static final double ROUNDING_TOLERANCE = 1e-6;

	public static void main(String[] args) {
		logger.info("Application is starting...");

		double expected = nbAccounts * initialBalance;
		System.out.printf("%d accounts x %.2f riyals = %.2f riyals in the system%n",
				nbAccounts, initialBalance, expected);

		runScenario("WITHOUT synchronization", UnsafeCryptoAccount::new, expected);
		runScenario("WITH synchronization", CryptoAccount::new, expected);

		logger.info("Application is closing...");
	}

	private static void runScenario(String label,
			BiFunction<String, Double, CryptoAccount> accountFactory, double expected) {

		heading(label);

		List<CryptoAccount> accounts = new ArrayList<>();
		for (int i = 0; i < nbAccounts; i++) {
			accounts.add(accountFactory.apply("acc_" + i, initialBalance));
		}

		System.out.printf("  before : %.2f riyals%n", sumTotalMoneyInAccounts(accounts));

		long start = System.currentTimeMillis();
		List<Thread> threads = startTransactionThreads(accounts);
		waitFor(threads);
		long elapsed = System.currentTimeMillis() - start;

		double actual = sumTotalMoneyInAccounts(accounts);
		double drift = actual - expected;

		// Even a correct run does not land on exactly 10000.0: summing doubles in a
		// different order gives a different last bit. A race loses whole riyals; this
		// residue is ~1e-12, so the tolerance separates the two cleanly.
		boolean conserved = Math.abs(drift) < ROUNDING_TOLERANCE;

		System.out.printf("  after  : %.2f riyals%n", actual);
		System.out.printf("  drift  : %+.2f riyals  (%s)%n", drift,
				conserved ? "money conserved" : "MONEY INVENTED OR DESTROYED");
		System.out.printf("  exact  : %+.12f  (%s)%n", drift,
				conserved ? "floating-point residue only, not a lost transaction" : "a real loss");
		System.out.printf("  took   : %d ms for %d threads%n", elapsed, nbTransactions);

		if (conserved) {
			logger.info("{}: total conserved at {}", label, actual);
		} else {
			logger.error("{}: total drifted by {} (expected {}, got {})", label, drift, expected, actual);
		}
	}

	/** Creates and starts one thread per transaction, as the handout asks. */
	private static List<Thread> startTransactionThreads(List<CryptoAccount> accounts) {
		List<Thread> threads = new ArrayList<>(nbTransactions);

		for (int i = 0; i < nbTransactions; i++) {
			CryptoAccount randomA = accounts.get((int) (Math.random() * nbAccounts));
			CryptoAccount randomB = accounts.get((int) (Math.random() * nbAccounts));

			if (randomA == randomB) {
				continue;
			}

			Double randomAmount = Math.random() * 10 + 1;
			Transaction t = new Transaction(randomA, randomB, randomAmount);

			Thread thread = new Thread(t);
			threads.add(thread);
			thread.start();
		}

		return threads;
	}

	private static void waitFor(List<Thread> threads) {
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("Interrupted while waiting for transactions", e);
				return;
			}
		}
	}

	public static Double sumTotalMoneyInAccounts(List<CryptoAccount> accounts) {
		Double total = 0.0;
		for (CryptoAccount account : accounts) {
			total += account.getBalance();
		}
		return total;
	}

	private static void heading(String title) {
		System.out.println();
		System.out.println("== " + title + " ==");
	}
}
