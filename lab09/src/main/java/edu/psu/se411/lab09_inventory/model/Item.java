package edu.psu.se411.lab09_inventory.model;

import java.util.Objects;

import edu.psu.se411.lab09_inventory.util.Validate;

public abstract class Item {

	private final String id;
	private final String name;

	protected Item(String id, String name) {
		this.id = Validate.requireText(id, "Item id");
		this.name = Validate.requireText(name, "Item name");
	}

	public abstract String getCategoryLabel();

	public abstract String describeDetails();

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Item item)) {
			return false;
		}
		return id.equals(item.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return String.format("%-6s %-14s %-32s %s", id, getCategoryLabel(), name, describeDetails());
	}
}
