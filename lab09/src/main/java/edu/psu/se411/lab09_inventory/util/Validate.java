package edu.psu.se411.lab09_inventory.util;

public final class Validate {

	public static String requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " is required");
		}
		return value.trim();
	}

	private Validate() {
		throw new AssertionError("Validate is a utility class and must not be instantiated");
	}
}
