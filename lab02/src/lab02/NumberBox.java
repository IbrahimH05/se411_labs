// Exercise 2
package lab02;

import java.util.List;

public class NumberBox<T extends Number> {

    private T item;

    public NumberBox() {
    }

    public NumberBox(T item) {
        this.item = item;
    }

    public void setItem(T item) {
        this.item = item;
    }

    public T getItem() {
        return item;
    }

    public double add(Number other) {
        return item.doubleValue() + other.doubleValue();
    }

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
