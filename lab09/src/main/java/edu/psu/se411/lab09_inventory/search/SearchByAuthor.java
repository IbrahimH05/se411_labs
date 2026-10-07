package edu.psu.se411.lab09_inventory.search;

import java.util.Locale;

import edu.psu.se411.lab09_inventory.model.Book;
import edu.psu.se411.lab09_inventory.util.Validate;

public class SearchByAuthor extends TypedSearchStrategy<Book> {

	private final String fragment;

	public SearchByAuthor(String fragment) {
		super(Book.class);
		this.fragment = Validate.requireText(fragment, "Search author").toLowerCase(Locale.ROOT);
	}

	@Override
	protected boolean matchesTyped(Book book) {
		return book.getAuthor().toLowerCase(Locale.ROOT).contains(fragment);
	}
}
