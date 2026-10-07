package edu.psu.se411.lab09_inventory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.psu.se411.lab09_inventory.model.Book;
import edu.psu.se411.lab09_inventory.model.ElectronicDevice;
import edu.psu.se411.lab09_inventory.model.Item;
import edu.psu.se411.lab09_inventory.search.AllOf;
import edu.psu.se411.lab09_inventory.search.SearchByAuthor;
import edu.psu.se411.lab09_inventory.search.SearchByCategory;
import edu.psu.se411.lab09_inventory.search.SearchById;
import edu.psu.se411.lab09_inventory.search.SearchByName;
import edu.psu.se411.lab09_inventory.search.SearchStrategy;

public class InventoryTest {

	private Inventory<Item> inventory;
	private Book concurrency;
	private Book effectiveJava;
	private ElectronicDevice laptop;

	@BeforeEach
	public void setUp() {
		inventory = new Inventory<>();
		concurrency = new Book("B-001", "Concurrency in Java", "Brian Goetz");
		effectiveJava = new Book("B-002", "Effective Java", "Joshua Bloch");
		laptop = new ElectronicDevice("E-001", "ThinkPad X1 Carbon", "Laptop");

		inventory.add(concurrency);
		inventory.add(effectiveJava);
		inventory.add(laptop);
	}

	// Part 1
	@Test
	public void items_can_be_added_and_counted() {
		assertEquals(3, inventory.size());
		assertFalse(inventory.isEmpty());
		assertTrue(inventory.getItems().containsAll(List.of(concurrency, effectiveJava, laptop)));
	}

	@Test
	public void an_item_can_be_removed() {
		assertTrue(inventory.remove(effectiveJava));
		assertEquals(2, inventory.size());
		assertFalse(inventory.getItems().contains(effectiveJava));
	}

	@Test
	public void removing_something_absent_reports_false() {
		assertFalse(inventory.remove(new Book("B-999", "Nope", "Nobody")));
		assertEquals(3, inventory.size());
	}

	@Test
	public void a_null_item_is_rejected() {
		assertThrows(IllegalArgumentException.class, () -> inventory.add(null));
	}

	@Test
	public void the_returned_list_cannot_be_modified_from_outside() {
		assertThrows(UnsupportedOperationException.class, () -> inventory.getItems().clear());
	}

	@Test
	public void one_inventory_holds_both_product_types() {
		assertEquals(2, inventory.findItems(item -> item instanceof Book).size());
		assertEquals(1, inventory.findItems(item -> item instanceof ElectronicDevice).size());
	}

	@Test
	public void items_are_identified_by_id() {
		assertEquals(concurrency, new Book("B-001", "Different title", "Different author"));
		assertEquals(concurrency.hashCode(), new Book("B-001", "x", "y").hashCode());
	}

	@Test
	public void blank_fields_are_rejected() {
		assertThrows(IllegalArgumentException.class, () -> new Book("", "Name", "Author"));
		assertThrows(IllegalArgumentException.class, () -> new Book("B-1", "  ", "Author"));
		assertThrows(IllegalArgumentException.class, () -> new ElectronicDevice("E-1", "Name", null));
	}

	// Part 2
	@Test
	public void search_by_name_finds_the_exact_title_from_the_handout() {
		List<Item> matches = inventory.findItems(new SearchByName("Concurrency in Java"));

		assertEquals(1, matches.size());
		assertEquals(concurrency, matches.get(0));
	}

	@Test
	public void search_by_name_is_a_case_insensitive_fragment_match() {
		assertEquals(2, inventory.findItems(new SearchByName("java")).size());
		assertEquals(2, inventory.findItems(new SearchByName("JAVA")).size());
	}

	@Test
	public void search_by_id_finds_one_item() {
		List<Item> matches = inventory.findItems(new SearchById("E-001"));

		assertEquals(1, matches.size());
		assertEquals(laptop, matches.get(0));
	}

	@Test
	public void search_by_author_finds_only_books_by_that_author() {
		List<Item> matches = inventory.findItems(new SearchByAuthor("Joshua Bloch"));

		assertEquals(1, matches.size());
		assertEquals(effectiveJava, matches.get(0));
	}

	@Test
	public void search_by_category_finds_only_devices_in_that_category() {
		List<Item> matches = inventory.findItems(new SearchByCategory("Laptop"));

		assertEquals(1, matches.size());
		assertEquals(laptop, matches.get(0));
	}

	@Test
	public void a_book_strategy_never_matches_an_electronic_device() {
		// The laptop's name contains no author, but the point is that the type check
		// runs first: a Book strategy must not throw or match on a device.
		assertDoesNotThrow(() -> inventory.findItems(new SearchByAuthor("ThinkPad")));
		assertTrue(inventory.findItems(new SearchByAuthor("ThinkPad")).isEmpty());
	}

	@Test
	public void a_device_strategy_never_matches_a_book() {
		assertTrue(inventory.findItems(new SearchByCategory("Concurrency in Java")).isEmpty());
	}

	@Test
	public void a_search_that_matches_nothing_returns_an_empty_list() {
		assertTrue(inventory.findItems(new SearchByName("zzzz")).isEmpty());
	}

	@Test
	public void a_null_strategy_is_rejected() {
		assertThrows(IllegalArgumentException.class, () -> inventory.findItems(null));
	}

	@Test
	public void strategies_compose_with_and() {
		inventory.add(new Book("B-003", "Java Puzzlers", "Joshua Bloch"));

		List<Item> matches = inventory.findItems(
				new AllOf<Item>(new SearchByAuthor("Bloch"), new SearchByName("java")));

		assertEquals(2, matches.size());
	}

	/** The reason findItems takes {@code SearchStrategy<? super T>} and not {@code <T>}. */
	@Test
	public void an_item_strategy_can_search_a_narrower_book_inventory() {
		Inventory<Book> booksOnly = new Inventory<>();
		booksOnly.add(new Book("B-010", "Java Concurrency in Practice", "Brian Goetz"));
		booksOnly.add(new Book("B-011", "Clean Code", "Robert Martin"));

		SearchStrategy<Item> byGoetz = new SearchByAuthor("Goetz");

		// Same strategy object, two differently-typed inventories.
		assertEquals(1, booksOnly.findItems(byGoetz).size());
		assertEquals(1, inventory.findItems(byGoetz).size());
	}

	@Test
	public void a_strategy_can_be_written_as_a_lambda() {
		SearchStrategy<Item> cheapTrick = item -> item.getId().startsWith("B-");
		assertEquals(2, inventory.findItems(cheapTrick).size());
	}

	// Part 3
	@Test
	public void display_inventory_accepts_any_element_type() {
		Inventory<String> spareParts = new Inventory<>();
		spareParts.add("USB-C cable");

		assertDoesNotThrow(() -> {
			App.displayInventory(inventory);
			App.displayInventory(spareParts);
			App.displayInventory(new Inventory<Integer>());
		});
	}

	@Test
	public void display_inventory_prints_every_item() {
		PrintStream original = System.out;
		ByteArrayOutputStream captured = new ByteArrayOutputStream();

		try {
			System.setOut(new PrintStream(captured));
			App.displayInventory(inventory);
		} finally {
			System.setOut(original);
		}

		String printed = captured.toString();
		assertTrue(printed.contains("Concurrency in Java"));
		assertTrue(printed.contains("Brian Goetz"));
		assertTrue(printed.contains("ThinkPad X1 Carbon"));
		assertTrue(printed.contains("category Laptop"));
	}
}
