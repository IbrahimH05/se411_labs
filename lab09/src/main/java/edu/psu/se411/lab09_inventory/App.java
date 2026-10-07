package edu.psu.se411.lab09_inventory;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.se411.lab09_inventory.model.Book;
import edu.psu.se411.lab09_inventory.model.ElectronicDevice;
import edu.psu.se411.lab09_inventory.model.Item;
import edu.psu.se411.lab09_inventory.search.AllOf;
import edu.psu.se411.lab09_inventory.search.SearchByAuthor;
import edu.psu.se411.lab09_inventory.search.SearchByCategory;
import edu.psu.se411.lab09_inventory.search.SearchById;
import edu.psu.se411.lab09_inventory.search.SearchByName;
import edu.psu.se411.lab09_inventory.search.SearchStrategy;

public class App {

	static Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		logger.info("Application is starting...");

		try {
			Inventory<Item> inventory = buildInventory();

			displayInventory(inventory);
			runSearches(inventory);
			showTypeSafety(inventory);
			showWildcardReach();
		} catch (RuntimeException e) {
			logger.error("Inventory system failed", e);
		}

		logger.info("Application is closing...");
	}

	// Part 1
	private static Inventory<Item> buildInventory() {
		Inventory<Item> inventory = new Inventory<>();

		inventory.add(new Book("B-001", "Concurrency in Java", "Brian Goetz"));
		inventory.add(new Book("B-002", "Effective Java", "Joshua Bloch"));
		inventory.add(new Book("B-003", "Java Puzzlers", "Joshua Bloch"));
		inventory.add(new ElectronicDevice("E-001", "ThinkPad X1 Carbon", "Laptop"));
		inventory.add(new ElectronicDevice("E-002", "Dell UltraSharp 27", "Monitor"));
		inventory.add(new ElectronicDevice("E-003", "MacBook Air M3", "Laptop"));

		logger.info("Inventory built with {} items", inventory.size());
		return inventory;
	}

	// Part 3
	public static void displayInventory(Inventory<?> inventory) {
		heading("Inventory contents (" + inventory.size() + " items)");

		for (Object item : inventory.getItems()) {
			System.out.println("  " + item);
		}
	}

	// Part 2
	private static void runSearches(Inventory<Item> inventory) {
		heading("Searching with strategies");

		report(inventory, "by name  \"Concurrency in Java\"", new SearchByName("Concurrency in Java"));
		report(inventory, "by name  \"java\"", new SearchByName("java"));
		report(inventory, "by id    \"E-002\"", new SearchById("E-002"));
		report(inventory, "by author \"Joshua Bloch\"", new SearchByAuthor("Joshua Bloch"));
		report(inventory, "by category \"Laptop\"", new SearchByCategory("Laptop"));

		// Strategies compose, so a new query needs no new class.
		report(inventory, "books by Bloch whose name contains \"java\"",
				new AllOf<Item>(new SearchByAuthor("Bloch"), new SearchByName("java")));
	}

	private static void report(Inventory<Item> inventory, String label, SearchStrategy<? super Item> strategy) {
		List<Item> matches = inventory.findItems(strategy);

		System.out.printf("%n  %s -> %d match(es)%n", label, matches.size());
		for (Item match : matches) {
			System.out.println("      " + match);
		}

		logger.info("Search [{}] matched {} item(s)", label.trim(), matches.size());
	}

	/** A Book-only strategy works against an Inventory<Book> too, thanks to {@code ? super T}. */
	private static void showTypeSafety(Inventory<Item> mixed) {
		heading("The same strategy object against a narrower inventory");

		Inventory<Book> booksOnly = new Inventory<>();
		booksOnly.add(new Book("B-010", "Java Concurrency in Practice", "Brian Goetz"));
		booksOnly.add(new Book("B-011", "Clean Code", "Robert Martin"));

		SearchByAuthor byGoetz = new SearchByAuthor("Goetz");

		System.out.printf("  Inventory<Item> : %d match(es)%n", mixed.findItems(byGoetz).size());
		System.out.printf("  Inventory<Book> : %d match(es)   (same strategy object)%n",
				booksOnly.findItems(byGoetz).size());
		System.out.println("  findItems(SearchStrategy<? super T>) is what allows both");
	}

	/** displayInventory takes Inventory<?>, so it is not limited to Item at all. */
	private static void showWildcardReach() {
		heading("displayInventory(Inventory<?>) on a completely unrelated type");

		Inventory<String> spareParts = new Inventory<>();
		spareParts.add("USB-C cable");
		spareParts.add("Laptop charger");

		displayInventory(spareParts);
	}

	private static void heading(String title) {
		System.out.println();
		System.out.println("== " + title + " ==");
	}
}
