package edu.psu.se411.lab09_inventory.model;

import edu.psu.se411.lab09_inventory.util.Validate;

public class ElectronicDevice extends Item {

	private final String category;

	public ElectronicDevice(String id, String name, String category) {
		super(id, name);
		this.category = Validate.requireText(category, "Category");
	}

	public String getCategory() {
		return category;
	}

	@Override
	public String getCategoryLabel() {
		return "Electronics";
	}

	@Override
	public String describeDetails() {
		return "category " + category;
	}
}
