// Exercise 1
package edu.psu.se411.exceptions;

public class InvalidAgeException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int age;
    private final int minimumAge;

    public InvalidAgeException(int age, int minimumAge) {
        super("Invalid age: " + age + ". Minimum accepted age is " + minimumAge + ".");
        this.age = age;
        this.minimumAge = minimumAge;
    }

    public int getAge() {
        return age;
    }

    public int getMinimumAge() {
        return minimumAge;
    }
}
