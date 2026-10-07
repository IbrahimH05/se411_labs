package edu.psu.se411.lab09_inventory.search;

@FunctionalInterface
public interface SearchStrategy<T> {

	boolean matches(T item);
}
