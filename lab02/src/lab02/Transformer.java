package lab02;

/**
 * Exercise 3 — a single transformation step, generic over both its input and
 * output types.
 *
 * <p>Because {@code T} and {@code R} are independent, one interface covers both
 * kinds of step the pipeline needs:
 *
 * <ul>
 *   <li>type-preserving, when {@code R} is the same as {@code T} —
 *       {@code Transformer<String, String>} (trim, upper-case)</li>
 *   <li>type-changing, when it is not —
 *       {@code Transformer<String, Integer>} (parse, measure length)</li>
 * </ul>
 *
 * <p>A single abstract method makes this a functional interface, so any
 * implementation can be written as a lambda.
 */
@FunctionalInterface
public interface Transformer<T, R> {

    /** Turns an input of type {@code T} into an output of type {@code R}. */
    R transform(T input);
}
