package edu.psu.se411.lab09_inventory.search;

import java.util.Locale;

import edu.psu.se411.lab09_inventory.model.Item;
import edu.psu.se411.lab09_inventory.util.Validate;

public class SearchByName implements SearchStrategy<Item> {

	private final String fragment;

	public SearchByName(String fragment) {
		this.fragment = Validate.requireText(fragment, "Search name").toLowerCase(Locale.ROOT);
	}

	@Override
	public boolean matches(Item item) {
		return item != null && item.getName().toLowerCase(Locale.ROOT).contains(fragment);
	}
}
