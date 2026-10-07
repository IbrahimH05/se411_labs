package edu.psu.se411.lab09_inventory.search;

import edu.psu.se411.lab09_inventory.model.ElectronicDevice;
import edu.psu.se411.lab09_inventory.util.Validate;

public class SearchByCategory extends TypedSearchStrategy<ElectronicDevice> {

	private final String category;

	public SearchByCategory(String category) {
		super(ElectronicDevice.class);
		this.category = Validate.requireText(category, "Search category");
	}

	@Override
	protected boolean matchesTyped(ElectronicDevice device) {
		return device.getCategory().equalsIgnoreCase(category);
	}
}
