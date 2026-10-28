package edu.psu.se411.lab12_threads;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.se411.lab12_threads.Simulation.Result;

/**
 * Main program 2: a CACHED thread pool.
 *
 * Run with: mvn exec:java -Dexec.mainClass=edu.spu... (see README)
 */
public class AppCachedPool {

	static Logger logger = LoggerFactory.getLogger(AppCachedPool.class);

	public static Integer nbAccounts = Integer.getInteger("sim.accounts", 10);
	public static Integer nbTransactions = Integer.getInteger("sim.transactions", 100000);

	public static void main(String[] args) {
		logger.info("Application is starting...");

		System.out.printf("%nCACHED thread pool (grows on demand, unbounded)%n");

		Result result = Simulation.run("cached",
				Executors.newCachedThreadPool(), nbAccounts, nbTransactions);

		Simulation.printResult(result);
		logger.info("Cached pool: {} accounts, {} txns, {} ms, peak {} threads",
				result.accounts(), result.transactions(), result.millis(), result.peakThreads());

		logger.info("Application is closing...");
	}
}
