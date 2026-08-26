package lab02;

import java.util.List;

/**
 * Exercise 2 — a wrapper whose type parameter is <em>bounded</em>:
 * {@code T extends Number}.
 *
 * <p>The bound is what makes this class useful. Without it, {@code T} could be
 * anything and the compiler would only let us call {@code Object} methods on the
 * stored value. Because {@code T} is guaranteed to be a {@code Number}, the body
 * can call {@link Number#doubleValue()} directly. In exchange,
 * {@code new NumberBox<String>()} is a compile error.
 */
public class NumberBox<T extends Number> {

    private T item;

    public NumberBox() {
    }

    public NumberBox(T item) {
        this.item = item;
    }

    /** Stores an item of type {@code T} in the wrapper. */
    public void setItem(T item) {
        this.item = item;
    }

    /** Retrieves the stored item. */
    public T getItem() {
        return item;
    }

    /**
     * Adds another number to the stored one.
     *
     * <p>The parameter is {@code Number}, not {@code T}, so an
     * {@code NumberBox<Integer>} can still be added to a {@code Double}. The
     * result is a {@code double} because that is the only primitive wide enough
     * to hold every {@code Number} subtype without losing information about the
     * magnitude.
     */
    public double add(Number other) {
        return item.doubleValue() + other.doubleValue();
    }

    /**
     * Sums a list of numbers, ignoring the box's own contents.
     *
     * <p>{@code List<? extends Number>} — an upper-bounded wildcard — is what
     * lets this accept a {@code List<Integer>} <em>and</em> a
     * {@code List<Double>}. A plain {@code List<Number>} parameter would accept
     * neither, because generics are invariant: {@code List<Integer>} is not a
     * subtype of {@code List<Number>}.
     */
    public static double sum(List<? extends Number> numbers) {
        double total = 0;
        for (Number number : numbers) {
            total += number.doubleValue();
        }
        return total;
    }

    @Override
    public String toString() {
        return "NumberBox(" + item + ")";
    }
}
