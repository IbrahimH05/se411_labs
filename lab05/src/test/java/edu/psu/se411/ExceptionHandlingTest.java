package edu.psu.se411;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.psu.se411.exceptions.InsufficientFundsException;
import edu.psu.se411.exceptions.InvalidAgeException;
import edu.psu.se411.model.BankAccount;
import edu.psu.se411.model.Wallet;

public class ExceptionHandlingTest {

    private Wallet wallet;
    private BankAccount account;

    @BeforeEach
    public void setUp() {
        wallet = new Wallet("Ibrahim", new BigDecimal("500.00"));
        account = new BankAccount("SA-1383-0001", new BigDecimal("1200.00"));
    }

    // Exercise 1
    @Test
    public void under_age_throws_and_carries_the_offending_age() {
        InvalidAgeException thrown = assertThrows(
                InvalidAgeException.class,
                () -> App.validateAge(15));

        assertEquals(15, thrown.getAge());
        assertEquals(18, thrown.getMinimumAge());
    }

    // Exercise 1
    @Test
    public void exactly_eighteen_is_accepted() {
        assertDoesNotThrow(() -> App.validateAge(18));
    }

    // Exercise 1
    @Test
    public void seventeen_is_rejected() {
        assertThrows(InvalidAgeException.class, () -> App.validateAge(17));
    }

    // Exercise 2
    @Test
    public void successful_withdrawal_moves_money_between_both_sides() throws Exception {
        wallet.withdrawToBank(new BigDecimal("120.50"), account);

        assertEquals(new BigDecimal("379.50"), wallet.getBalance());
        assertEquals(new BigDecimal("1320.50"), account.getBalance());
    }

    // Exercise 2
    @Test
    public void overdraw_throws_and_reports_the_shortfall() {
        InsufficientFundsException thrown = assertThrows(
                InsufficientFundsException.class,
                () -> wallet.withdrawToBank(new BigDecimal("900.00"), account));

        assertEquals(new BigDecimal("900.00"), thrown.getRequested());
        assertEquals(new BigDecimal("500.00"), thrown.getAvailable());
        assertEquals(new BigDecimal("400.00"), thrown.getShortfall());
    }

    // Exercise 2
    @Test
    public void failed_withdrawal_leaves_both_balances_untouched() {
        assertThrows(
                InsufficientFundsException.class,
                () -> wallet.withdrawToBank(new BigDecimal("900.00"), account));

        assertEquals(new BigDecimal("500.00"), wallet.getBalance());
        assertEquals(new BigDecimal("1200.00"), account.getBalance());
    }

    // Exercise 2
    @Test
    public void withdrawing_the_entire_balance_is_allowed() throws Exception {
        wallet.withdrawToBank(new BigDecimal("500.00"), account);

        assertEquals(0, wallet.getBalance().signum());
    }

    // Exercise 2
    @Test
    public void non_positive_amounts_are_programming_errors_not_business_failures() {
        assertThrows(IllegalArgumentException.class,
                () -> wallet.withdrawToBank(new BigDecimal("-50.00"), account));
        assertThrows(IllegalArgumentException.class,
                () -> wallet.withdrawToBank(BigDecimal.ZERO, account));
        assertThrows(IllegalArgumentException.class,
                () -> wallet.withdrawToBank(new BigDecimal("10.00"), null));
    }

    // Exercise 2
    @Test
    public void a_wallet_cannot_open_with_a_negative_balance() {
        assertThrows(IllegalArgumentException.class,
                () -> new Wallet("Ibrahim", new BigDecimal("-1.00")));
    }
}
