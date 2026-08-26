// Exercise 3
package lab02;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pipeline<T, R> {

    private final List<Transformer<?, ?>> transformers;

    private Pipeline(List<Transformer<?, ?>> transformers) {
        this.transformers = Collections.unmodifiableList(transformers);
    }

    public static <T> Pipeline<T, T> start(Class<T> inputType) {
        return new Pipeline<>(new ArrayList<>());
    }

    public <V> Pipeline<T, V> add(Transformer<? super R, ? extends V> transformer) {
        List<Transformer<?, ?>> extended = new ArrayList<>(transformers);
        extended.add(transformer);
        return new Pipeline<>(extended);
    }

    @SuppressWarnings("unchecked")
    public R execute(T input) {
        Object current = input;
        for (Transformer<?, ?> transformer : transformers) {
            current = ((Transformer<Object, Object>) transformer).transform(current);
        }
        return (R) current;
    }

    public int size() {
        return transformers.size();
    }
}
