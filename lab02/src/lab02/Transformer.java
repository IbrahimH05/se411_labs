// Exercise 3
package lab02;

@FunctionalInterface
public interface Transformer<T, R> {

    R transform(T input);
}
