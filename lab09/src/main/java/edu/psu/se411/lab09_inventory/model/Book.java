package edu.psu.se411.lab09_inventory.model;

import edu.psu.se411.lab09_inventory.util.Validate;

public class Book extends Item {

	private final String author;

	public Book(String id, String name, String author) {
		super(id, name);
		this.author = Validate.requireText(author, "Author");
	}

	public String getAuthor() {
		return author;
	}

	@Override
	public String getCategoryLabel() {
		return "Book";
	}

	@Override
	public String describeDetails() {
		return "by " + author;
	}
}
