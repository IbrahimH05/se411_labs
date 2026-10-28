package edu.psu.se411.lab12_threads;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.se411.lab12_threads.Simulation.Result;

/**
 * Main program 1: a FIXED thread pool.
 *
 * Build and run using Maven goal: clean package exec:java
 * Override with -Dsim.accounts=1000 -Dsim.transactions=1000000
 */
public class App {

	static Logger logger = LoggerFactory.getLogger(App.class);

	public static Integer nbAccounts = Integer.getInteger("sim.accounts", 10);
	public static Integer nbTransactions = Integer.getInteger("sim.transactions", 100000);

	/**
	 * The starter sized this pool at nbTransactions. That is one thread per task:
	 * at a million transactions the JVM would try to create a million threads and
	 * run out of memory long before it finished. A fixed pool exists to cap
	 * concurrency, so it is sized to the hardware.
	 */
	public static Integer poolSize = Integer.getInteger("sim.poolsize",
			Runtime.getRuntime().availableProcessors());

	public static void main(String[] args) {
		logger.info("Application is starting...");

		System.out.printf("%nFIXED thread pool of %d (available processors: %d)%n",
				poolSize, Runtime.getRuntime().availableProcessors());

		Result result = Simulation.run("fixed(" + poolSize + ")",
				Executors.newFixedThreadPool(poolSize), nbAccounts, nbTransactions);

		Simulation.printResult(result);
		logger.info("Fixed pool: {} accounts, {} txns, {} ms, peak {} threads",
				result.accounts(), result.transactions(), result.millis(), result.peakThreads());

		logger.info("Application is closing...");
	}
}
