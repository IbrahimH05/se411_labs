package edu.psu.se411.lab09_inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.se411.lab09_inventory.search.SearchStrategy;

public class Inventory<T> {

	private static final Logger logger = LoggerFactory.getLogger(Inventory.class);

	private final List<T> items = new ArrayList<>();

	public void add(T item) {
		if (item == null) {
			throw new IllegalArgumentException("Cannot add a null item to the inventory");
		}
		items.add(item);
		logger.debug("Added {} ({} items in stock)", item, items.size());
	}

	public boolean remove(T item) {
		boolean removed = items.remove(item);
		logger.debug("Remove {} -> {} ({} items in stock)", item, removed ? "removed" : "not found", items.size());
		return removed;
	}

	public List<T> getItems() {
		return Collections.unmodifiableList(items);
	}

	/**
	 * {@code ? super T} rather than {@code SearchStrategy<T>}: the strategy only
	 * consumes items, so one written against any supertype of T is still safe. That
	 * is what lets a {@code SearchStrategy<Item>} search an {@code Inventory<Book>}.
	 */
	public List<T> findItems(SearchStrategy<? super T> strategy) {
		if (strategy == null) {
			throw new IllegalArgumentException("A search strategy is required");
		}

		List<T> matches = new ArrayList<>();
		for (T item : items) {
			if (strategy.matches(item)) {
				matches.add(item);
			}
		}

		logger.debug("Search matched {} of {} items", matches.size(), items.size());
		return matches;
	}

	public int size() {
		return items.size();
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}
}
