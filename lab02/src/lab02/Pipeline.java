package lab02;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exercise 3 — an immutable chain of {@link Transformer}s.
 *
 * <p>The two type parameters play different roles, and that is the whole trick:
 *
 * <ul>
 *   <li>{@code T} — the type the pipeline is fed. Fixed when the pipeline is
 *       created and never changes.</li>
 *   <li>{@code R} — the type the pipeline currently produces. Changes every time
 *       a transformer is appended.</li>
 * </ul>
 *
 * <p>Since a type parameter cannot be reassigned, {@link #add} cannot mutate
 * {@code R} in place. Instead it returns a <em>new</em> {@code Pipeline<T, V>}
 * carrying the new output type. Chaining {@code add} calls therefore walks the
 * type forward at compile time:
 *
 * <pre>{@code
 * Pipeline.start(String.class)      // Pipeline<String, String>
 *         .add(String::trim)        // Pipeline<String, String>
 *         .add(String::length)      // Pipeline<String, Integer>
 *         .add(n -> n * 2.5)        // Pipeline<String, Double>
 *         .execute("  hello  ");    // Double
 * }</pre>
 *
 * <p>The compiler tracks the intermediate types for us: appending a
 * {@code Transformer<Integer, ?>} to a pipeline that currently produces
 * {@code String} will not compile.
 */
public class Pipeline<T, R> {

    /**
     * The steps, in application order.
     *
     * <p>The element type has to be the raw-ish {@code Transformer<?, ?>}: the
     * intermediate types between steps are not expressible as a single type
     * argument, and they are not needed at runtime — only the chain's endpoints
     * ({@code T} and {@code R}) are visible to callers. {@link #add} is the only
     * way to extend the list, and it is what guarantees each step's input
     * matches the previous step's output.
     */
    private final List<Transformer<?, ?>> transformers;

    private Pipeline(List<Transformer<?, ?>> transformers) {
        this.transformers = Collections.unmodifiableList(transformers);
    }

    /**
     * Creates an empty pipeline over the initial type {@code T}.
     *
     * <p>With no steps yet, the output type equals the input type, hence
     * {@code Pipeline<T, T>}. The {@code Class<T>} argument is only there to let
     * the compiler infer {@code T} at the call site; it is not stored.
     */
    public static <T> Pipeline<T, T> start(Class<T> inputType) {
        return new Pipeline<>(new ArrayList<>());
    }

    /**
     * Appends a transformer and returns a new pipeline whose output type is
     * {@code V}.
     *
     * <p>{@code Transformer<? super R, ? extends V>} rather than
     * {@code Transformer<R, V>} follows the PECS rule: the step consumes
     * {@code R}, so anything that can handle an {@code R} or a supertype of it
     * will do, and it produces something assignable to {@code V}. That makes a
     * {@code Transformer<Object, String>} usable as a step on a pipeline of
     * {@code Integer}.
     *
     * <p>The receiver is left untouched — a pipeline can safely be branched by
     * calling {@code add} on it more than once.
     */
    public <V> Pipeline<T, V> add(Transformer<? super R, ? extends V> transformer) {
        List<Transformer<?, ?>> extended = new ArrayList<>(transformers);
        extended.add(transformer);
        return new Pipeline<>(extended);
    }

    /**
     * Runs every transformer in order and returns the final result.
     *
     * <p>The unchecked casts are unavoidable and safe. Unavoidable, because the
     * intermediate values have no compile-time type here — the list has erased
     * them. Safe, because {@link #add} is the sole way to build the list, and its
     * signature already proved at compile time that step <i>n</i>'s output feeds
     * step <i>n+1</i>'s input, and that the last step produces {@code R}.
     */
    @SuppressWarnings("unchecked")
    public R execute(T input) {
        Object current = input;
        for (Transformer<?, ?> transformer : transformers) {
            current = ((Transformer<Object, Object>) transformer).transform(current);
        }
        return (R) current;
    }

    /** How many steps this pipeline will apply. */
    public int size() {
        return transformers.size();
    }
}
