package edu.spu.se411.lab06_logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.spu.se411.lab06_logging.exceptions.InsufficientFundsException;
import edu.spu.se411.lab06_logging.model.WalletAccount;

public class App {

	private static final Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {

		logger.info("Application is starting...");

		WalletAccount account = new WalletAccount(1000);

		try {
			account.withdraw(1500);
		} catch (InsufficientFundsException e) {
			logger.error("Withdrawal failed for amount {}", 1500, e);
		}

		try {
			account.deposit(-100);
		} catch (IllegalArgumentException e) {
			logger.error("Deposit failed for amount {}", -100, e);
		}

		account.deposit(250);

		try {
			account.withdraw(400);
		} catch (InsufficientFundsException e) {
			logger.error("Withdrawal failed for amount {}", 400, e);
		}

		logger.info("Application is ending...");
	}

}
