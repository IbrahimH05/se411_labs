package edu.psu.se411.lab09_inventory.search;

import java.util.List;

/** Combines strategies with AND, so new searches need no new class. */
public class AllOf<T> implements SearchStrategy<T> {

	private final List<SearchStrategy<? super T>> strategies;

	@SafeVarargs
	public AllOf(SearchStrategy<? super T>... strategies) {
		if (strategies == null || strategies.length == 0) {
			throw new IllegalArgumentException("At least one strategy is required");
		}
		this.strategies = List.of(strategies);
	}

	@Override
	public boolean matches(T item) {
		for (SearchStrategy<? super T> strategy : strategies) {
			if (!strategy.matches(item)) {
				return false;
			}
		}
		return true;
	}
}
