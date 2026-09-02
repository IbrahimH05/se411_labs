package edu.psu.se411.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StackTest {

    private Stack<String> stringStack;

    @BeforeEach
    public void setUp() {
        stringStack = new Stack<String>();
    }

    // Step 5: push -> push -> pop returns the latest pushed element
    @Test
    public void push_push_pop_returns_last_pushed() {
        stringStack.push("Z");
        stringStack.push("A");

        assertEquals("A", stringStack.pop());
    }

    // Step 8: popping an empty stack raises NoSuchElementException
    @Test
    public void pop_empty_stack() {
        NoSuchElementException thrown = assertThrows(
                NoSuchElementException.class,
                () -> stringStack.pop(),
                "Expected pop from empty Stack to throw, but it didn't");

        assertTrue(thrown.getMessage().equals("Stack is empty, cannot pop"));
    }

    // Step 9: pushed elements are popped in reverse order
    @Test
    public void elements_pop_in_reverse_order() {
        stringStack.push("first");
        stringStack.push("second");
        stringStack.push("third");

        assertEquals("third", stringStack.pop());
        assertEquals("second", stringStack.pop());
        assertEquals("first", stringStack.pop());
    }

    @Test
    public void pop_removes_the_element_so_stack_empties() {
        stringStack.push("only");
        stringStack.pop();

        assertThrows(NoSuchElementException.class, () -> stringStack.pop());
    }

    @Test
    public void stack_is_generic_and_works_with_integers() {
        Stack<Integer> intStack = new Stack<Integer>(3);
        intStack.push(10);
        intStack.push(20);

        assertEquals(20, intStack.pop());
        assertEquals(10, intStack.pop());
    }

    @Test
    public void non_positive_capacity_falls_back_to_default() {
        Stack<String> zeroCapacity = new Stack<String>(0);
        zeroCapacity.push("still works");

        assertEquals("still works", zeroCapacity.pop());
    }
}
