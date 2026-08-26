package lab02;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Exercise 1 — a generic container that stores items in a {@link List} and can
 * print them.
 *
 * <p>{@code T} is a type parameter: the class is written once and works for any
 * reference type. {@code new PrintableList<>(new String[]{...})} gives a list of
 * Strings, and the compiler will reject an Integer being added to it.
 */
public class PrintableList<T> {

    private final List<T> items;

    /**
     * Builds the list from an array of items.
     *
     * <p>{@code Arrays.asList} wraps the array rather than copying it, and the
     * view it returns is fixed-size — {@code add} and {@code remove} throw. It
     * is copied into an {@code ArrayList} so the field behaves like a normal
     * list.
     */
    public PrintableList(T[] items) {
        this.items = new ArrayList<>(Arrays.asList(items));
    }

    /** Prints every item, one per line. */
    public void printItems() {
        for (T item : items) {
            System.out.println(item);
        }
    }

    /** The stored items, so callers can reuse them (used by Exercise 4). */
    public List<T> getItems() {
        return items;
    }

    public int size() {
        return items.size();
    }
}
