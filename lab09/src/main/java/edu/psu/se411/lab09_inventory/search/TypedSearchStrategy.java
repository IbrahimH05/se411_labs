package edu.psu.se411.lab09_inventory.search;

import edu.psu.se411.lab09_inventory.model.Item;

/**
 * A strategy for one concrete item type that is still usable against a mixed
 * inventory. It is a {@code SearchStrategy<Item>}, so it can be handed to an
 * {@code Inventory<Item>}; items of any other type simply do not match.
 *
 * <p>The narrowing is written once here rather than in every subclass.
 */
public abstract class TypedSearchStrategy<I extends Item> implements SearchStrategy<Item> {

	private final Class<I> type;

	protected TypedSearchStrategy(Class<I> type) {
		this.type = type;
	}

	@Override
	public final boolean matches(Item item) {
		return type.isInstance(item) && matchesTyped(type.cast(item));
	}

	protected abstract boolean matchesTyped(I item);
}
