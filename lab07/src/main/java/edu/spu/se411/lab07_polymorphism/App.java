package edu.spu.se411.lab07_polymorphism;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.spu.se411.lab07_polymorphism.exceptions.BookingException;
import edu.spu.se411.lab07_polymorphism.exceptions.InvalidArgumentException;
import edu.spu.se411.lab07_polymorphism.model.Booking;
import edu.spu.se411.lab07_polymorphism.model.CarRentalBooking;
import edu.spu.se411.lab07_polymorphism.model.FlightBooking;
import edu.spu.se411.lab07_polymorphism.model.SeatClass;
import edu.spu.se411.lab07_polymorphism.model.TrainBooking;
import edu.spu.se411.lab07_polymorphism.service.BookingService;

public class App {

	private static final Logger logger = LoggerFactory.getLogger(App.class);

	private static final BookingService service = new BookingService();

	public static void main(String[] args) {
		logger.info("Application is starting...");

		try {
			List<Booking> bookings = buildBookings();

			priceEveryBooking(bookings);
			showRevenue(bookings);
			demonstrateExceptions();
		} catch (RuntimeException e) {
			logger.error("Unexpected failure in the booking system", e);
		}

		logger.info("Application is stopping...");
	}

	private static List<Booking> buildBookings() {
		List<Booking> bookings = new ArrayList<>();

		FlightBooking flight = new FlightBooking("FL-1001", "Ibrahim Alhalaki",
				LocalDate.of(2026, 11, 12), "Istanbul", 1850.00);

		TrainBooking train = new TrainBooking("TR-2001", "Muhammad Abanmi",
				LocalDate.of(2026, 11, 20), "Riyadh", SeatClass.FIRST_CLASS);

		CarRentalBooking car = new CarRentalBooking("CR-3001", "Layla Nasser",
				LocalDate.of(2026, 12, 03), "Dubai", 145.00);

		TrainBooking pending = new TrainBooking("TR-2002", "Omar Khalid",
				LocalDate.of(2026, 12, 15), "Dammam", SeatClass.STANDARD);

		// Values the customer or the system supplies after the booking exists.
		try {
			flight.setLuggageWeightKg(23.5);
			train.setDistanceKm(395.0);
			car.setRentalDays(6);
		} catch (InvalidArgumentException e) {
			logger.error("Rejected a value while preparing the sample bookings", e);
		}

		bookings.add(flight);
		bookings.add(train);
		bookings.add(car);
		bookings.add(pending);

		logger.debug("Built {} sample bookings", bookings.size());
		return bookings;
	}

	// One loop, one call, three different price formulas - this is the polymorphism.
	private static void priceEveryBooking(List<Booking> bookings) {
		heading("Bookings priced through Booking.computeTotalPrice()");

		for (Booking booking : bookings) {
			try {
				double total = service.computeTotalPrice(booking);
				System.out.printf("  %-6s %-10s %-18s %10.2f %s%n",
						booking.getBookingId(), booking.getBookingType(),
						booking.getCustomerName(), total, "USD");
			} catch (BookingException e) {
				logger.error("Could not price booking {}", booking.getBookingId(), e);
				System.out.printf("  %-6s %-10s %-18s %s%n",
						booking.getBookingId(), booking.getBookingType(),
						booking.getCustomerName(), "no price: " + e.getMessage());
			}
		}
	}

	private static void showRevenue(List<Booking> bookings) {
		heading("Summary");

		List<Booking> priceable = service.findPriceable(bookings);
		System.out.printf("  priceable bookings : %d of %d%n", priceable.size(), bookings.size());
		System.out.printf("  total revenue      : %.2f USD%n", service.computeTotalRevenue(bookings));

		heading("Full manifest (Booking.summary(), shared by every subtype)");
		for (Booking booking : bookings) {
			System.out.println("  " + booking);
		}
	}

	private static void demonstrateExceptions() {
		heading("Business rules enforced");

		FlightBooking flight = new FlightBooking("FL-9001", "Test Passenger",
				LocalDate.of(2026, 11, 1), "Cairo", 900.00);

		attemptPrice(flight, "flight with no luggage weight entered");

		attemptSet("luggage weight of 55 kg (max is 40)", () -> flight.setLuggageWeightKg(55.0));

		TrainBooking train = new TrainBooking("TR-9001", "Test Passenger",
				LocalDate.of(2026, 11, 1), "Jeddah", SeatClass.STANDARD);

		attemptSet("train distance of 2500 km (max is 2000)", () -> train.setDistanceKm(2500.0));

		CarRentalBooking car = new CarRentalBooking("CR-9001", "Test Passenger",
				LocalDate.of(2026, 11, 1), "Doha", 120.00);

		attemptSet("rental of 0 days (min is 1)", () -> car.setRentalDays(0));
		attemptPrice(car, "car rental with no days entered");
	}

	private static void attemptPrice(Booking booking, String description) {
		try {
			service.computeTotalPrice(booking);
			System.out.printf("  %-45s -> accepted%n", description);
		} catch (BookingException e) {
			logger.error("Rejected: {}", description, e);
			System.out.printf("  %-45s -> %s%n", description, e.getClass().getSimpleName());
		}
	}

	private static void attemptSet(String description, ThrowingAction action) {
		try {
			action.run();
			System.out.printf("  %-45s -> accepted%n", description);
		} catch (BookingException e) {
			logger.error("Rejected: {}", description, e);
			System.out.printf("  %-45s -> %s%n", description, e.getClass().getSimpleName());
		}
	}

	@FunctionalInterface
	private interface ThrowingAction {
		void run() throws BookingException;
	}

	private static void heading(String title) {
		System.out.println();
		System.out.println("== " + title + " ==");
	}
}
