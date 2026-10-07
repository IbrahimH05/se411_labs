package edu.psu.se411.lab09_inventory.search;

import edu.psu.se411.lab09_inventory.model.Item;
import edu.psu.se411.lab09_inventory.util.Validate;

public class SearchById implements SearchStrategy<Item> {

	private final String id;

	public SearchById(String id) {
		this.id = Validate.requireText(id, "Search id");
	}

	@Override
	public boolean matches(Item item) {
		return item != null && item.getId().equalsIgnoreCase(id);
	}
}
