package edu.psu.se411.lab12_threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.psu.se411.lab12_threads.model.CryptoAccount;
import edu.psu.se411.lab12_threads.model.OrderedTransaction;

/**
 * The run itself, shared by both main programs so the executor is genuinely the
 * only variable being compared.
 */
public final class Simulation {

	public record Result(String poolLabel, int accounts, int transactions,
			long millis, double expectedTotal, double actualTotal, int peakThreads) {

		public boolean conserved() {
			return Math.abs(actualTotal - expectedTotal) < 1e-6;
		}

		public double throughputPerSecond() {
			return millis == 0 ? 0 : transactions / (millis / 1000.0);
		}
	}

	public static final double INITIAL_BALANCE = 1000.0;

	public static Result run(String poolLabel, ExecutorService executor, int nbAccounts, int nbTransactions) {
		List<CryptoAccount> accounts = new ArrayList<>(nbAccounts);
		for (int i = 0; i < nbAccounts; i++) {
			accounts.add(new CryptoAccount("acc_" + i, INITIAL_BALANCE));
		}

		double expected = sumTotalMoneyInAccounts(accounts);
		long start = System.currentTimeMillis();

		for (int i = 0; i < nbTransactions; i++) {
			int senderIndex = (int) (Math.random() * nbAccounts);
			int receiverIndex = (int) (Math.random() * nbAccounts);
			if (senderIndex == receiverIndex) {
				receiverIndex = (receiverIndex + 1) % nbAccounts;
			}

			CryptoAccount sender = accounts.get(senderIndex);
			CryptoAccount receiver = accounts.get(receiverIndex);

			executor.execute(new OrderedTransaction(sender, receiver, Math.random() * 100));
		}

		executor.shutdown();
		try {
			if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			executor.shutdownNow();
		}

		long millis = System.currentTimeMillis() - start;

		int peak = executor instanceof ThreadPoolExecutor pool ? pool.getLargestPoolSize() : -1;

		return new Result(poolLabel, nbAccounts, nbTransactions, millis,
				expected, sumTotalMoneyInAccounts(accounts), peak);
	}

	public static Double sumTotalMoneyInAccounts(List<CryptoAccount> accounts) {
		Double total = 0.0;
		for (CryptoAccount account : accounts) {
			total += account.getBalance();
		}
		return total;
	}

	public static void printResult(Result r) {
		System.out.printf("  %-14s %5d accounts  %9d txns  %7d ms  %10.0f txn/s  peak %6d threads  %s%n",
				r.poolLabel(), r.accounts(), r.transactions(), r.millis(),
				r.throughputPerSecond(), r.peakThreads(),
				r.conserved() ? "money conserved" : "TOTAL DRIFTED");
	}

	private Simulation() {
	}
}
