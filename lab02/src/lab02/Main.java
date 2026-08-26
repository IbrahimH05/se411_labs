package lab02;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        exercise1();
        exercise2();
        exercise3();
        exercise4();
    }

    // Exercise 1
    private static void exercise1() {
        heading("Exercise 1 — PrintableList<T>");

        String[] courses = {"SE411", "SE311", "CS201"};
        PrintableList<String> courseList = new PrintableList<>(courses);
        System.out.println("PrintableList<String>, " + courseList.size() + " items:");
        courseList.printItems();

        Integer[] years = {2024, 2025, 2026};
        PrintableList<Integer> yearList = new PrintableList<>(years);
        System.out.println("\nPrintableList<Integer>, " + yearList.size() + " items:");
        yearList.printItems();
    }

    // Exercise 2
    private static void exercise2() {
        heading("Exercise 2 — NumberBox<T extends Number>");

        NumberBox<Integer> intBox = new NumberBox<>(10);
        System.out.println("intBox           = " + intBox);
        System.out.println("intBox.getItem() = " + intBox.getItem());
        System.out.println("intBox.add(5)    = " + intBox.add(5));

        intBox.setItem(42);
        System.out.println("after setItem(42), getItem() = " + intBox.getItem());

        NumberBox<Double> doubleBox = new NumberBox<>(2.5);
        System.out.println("\ndoubleBox            = " + doubleBox);
        System.out.println("doubleBox.add(0.75)  = " + doubleBox.add(0.75));
        System.out.println("doubleBox.add(3)     = " + doubleBox.add(3));

        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
        List<Double> doubles = Arrays.asList(1.5, 2.5, 3.0);
        System.out.println("\nNumberBox.sum(" + integers + ") = " + NumberBox.sum(integers));
        System.out.println("NumberBox.sum(" + doubles + ") = " + NumberBox.sum(doubles));
    }

    // Exercise 3
    private static void exercise3() {
        heading("Exercise 3 — Pipeline<T, R>");

        Pipeline<String, String> trimmed =
                Pipeline.start(String.class)
                        .add(String::trim);

        Pipeline<String, String> shouted = trimmed.add(String::toUpperCase);

        Pipeline<String, Integer> lengthOf = shouted.add(String::length);

        Pipeline<String, Double> scaled = lengthOf.add(length -> length * 2.5);

        String input = "   generics are structural   ";
        System.out.println("input   = \"" + input + "\"");
        System.out.printf("%-26s -> \"%s\"%n", "trim            (String)", trimmed.execute(input));
        System.out.printf("%-26s -> \"%s\"%n", "+ upper         (String)", shouted.execute(input));
        System.out.printf("%-26s -> %s%n", "+ length       (Integer)", lengthOf.execute(input));
        System.out.printf("%-26s -> %s%n", "+ times 2.5     (Double)", scaled.execute(input));
        System.out.println("steps in the last pipeline = " + scaled.size());

        Pipeline<String, Boolean> isLong = lengthOf.add(length -> length > 20);
        System.out.println("\nbranching off the length pipeline instead:");
        System.out.printf("%-26s -> %s%n", "+ length > 20  (Boolean)", isLong.execute(input));
        System.out.println("original 4-step pipeline is unchanged, size = " + scaled.size());

        Pipeline<Integer, String> describe =
                Pipeline.start(Integer.class)
                        .add(n -> n * n)
                        .add(square -> "square = " + square);
        System.out.println("\ndescribe.execute(7) = \"" + describe.execute(7) + "\"");
    }

    // Exercise 4
    public static void printList(List<?> list) {
        for (Object item : list) {
            System.out.println("  " + item);
        }
    }

    // Exercise 4
    public static double sumNumbers(List<? extends Number> numbers) {
        double total = 0;
        for (Number number : numbers) {
            total += number.doubleValue();
        }
        return total;
    }

    // Exercise 4
    private static void exercise4() {
        heading("Exercise 4 — wildcards");

        List<String> words = Arrays.asList("wildcard", "bounded", "erasure");
        List<Integer> numbers = Arrays.asList(4, 8, 15, 16, 23, 42);
        List<Double> ratios = Arrays.asList(0.5, 1.25, 2.75);

        System.out.println("printList(List<String>):");
        printList(words);
        System.out.println("printList(List<Integer>):");
        printList(numbers);

        System.out.println("\nsumNumbers(" + numbers + ")  = " + sumNumbers(numbers));
        System.out.println("sumNumbers(" + ratios + ") = " + sumNumbers(ratios));
        System.out.println("sumNumbers(List<Long>)          = "
                + sumNumbers(Arrays.asList(100L, 200L, 300L)));
    }

    private static void heading(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
