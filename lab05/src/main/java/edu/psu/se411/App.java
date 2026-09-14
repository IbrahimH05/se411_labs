package edu.psu.se411;

import java.math.BigDecimal;

import edu.psu.se411.exceptions.InsufficientFundsException;
import edu.psu.se411.exceptions.InvalidAgeException;
import edu.psu.se411.model.BankAccount;
import edu.psu.se411.model.Wallet;

public class App {

    public static final int MINIMUM_AGE = 18;

    public static void main(String[] args) {
        runExercise1();
        runExercise2();
    }

    // Exercise 1
    public static void validateAge(int age) throws InvalidAgeException {
        if (age < MINIMUM_AGE) {
            throw new InvalidAgeException(age, MINIMUM_AGE);
        }
        System.out.println("Age valid message.");
    }

    // Exercise 1
    private static void runExercise1() {
        heading("Exercise 1 - custom checked exception");

        int[] agesToCheck = {25, 18, 15};

        for (int age : agesToCheck) {
            System.out.println("validateAge(" + age + "):");
            try {
                validateAge(age);
            } catch (InvalidAgeException e) {
                System.out.println("  rejected -> " + e.getMessage());
                System.out.println("  short by " + (e.getMinimumAge() - e.getAge()) + " years");
            }
        }
    }

    // Exercise 2
    private static void runExercise2() {
        heading("Exercise 2 - online wallet");

        Wallet wallet = new Wallet("Ibrahim", new BigDecimal("500.00"));
        BankAccount account = new BankAccount("SA-1383-0001", new BigDecimal("1200.00"));

        printBalances(wallet, account);

        transfer(wallet, account, new BigDecimal("120.50"));
        transfer(wallet, account, new BigDecimal("900.00"));
        transfer(wallet, account, new BigDecimal("379.50"));
    }

    // Exercise 2
    private static void transfer(Wallet wallet, BankAccount account, BigDecimal amount) {
        System.out.println("\nwithdrawToBank(" + amount + "):");
        try {
            wallet.withdrawToBank(amount, account);
            System.out.println("  transferred " + amount + " to " + account.getAccountNumber());
        } catch (InsufficientFundsException e) {
            System.out.println("  declined -> " + e.getMessage());
            System.out.println("  deposit at least " + e.getShortfall() + " to complete this withdrawal");
        }
        printBalances(wallet, account);
    }

    private static void printBalances(Wallet wallet, BankAccount account) {
        System.out.println("  wallet(" + wallet.getOwner() + ") = " + wallet.getBalance()
                + " | bank(" + account.getAccountNumber() + ") = " + account.getBalance());
    }

    private static void heading(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
