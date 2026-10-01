package edu.spu.se411.lab07_polymorphism.model;

import java.time.LocalDate;

import edu.spu.se411.lab07_polymorphism.config.TravelConfig;
import edu.spu.se411.lab07_polymorphism.exceptions.InvalidArgumentException;
import edu.spu.se411.lab07_polymorphism.exceptions.MissingInformationException;

public abstract class Booking {

	private final String bookingId;
	private final String customerName;
	private final LocalDate travelDate;
	private final String destinationCity;

	protected Booking(String bookingId, String customerName, LocalDate travelDate, String destinationCity) {
		this.bookingId = requireText(bookingId, "Booking ID");
		this.customerName = requireText(customerName, "Customer name");
		this.destinationCity = requireText(destinationCity, "Destination city");

		if (travelDate == null) {
			throw new IllegalArgumentException("Travel date is required");
		}
		this.travelDate = travelDate;
	}

	public abstract double computeTotalPrice() throws MissingInformationException, InvalidArgumentException;

	public abstract String getBookingType();

	public abstract String describeDetails();

	public final String summary() {
		String price;
		try {
			price = formatMoney(computeTotalPrice());
		} catch (MissingInformationException | InvalidArgumentException e) {
			price = "unavailable (" + e.getMessage() + ")";
		}

		return String.format("[%s] %s | %s | %s on %s | %s | total %s",
				bookingId, getBookingType(), customerName, destinationCity, travelDate, describeDetails(), price);
	}

	protected final void requireProvided(Object value, String fieldName) throws MissingInformationException {
		if (value == null) {
			throw new MissingInformationException(bookingId, fieldName);
		}
	}

	protected final void requireInRange(double value, double min, double max, String fieldName)
			throws InvalidArgumentException {
		if (value < min || value > max) {
			throw new InvalidArgumentException(bookingId, fieldName, value, min, max);
		}
	}

	protected static String formatMoney(double amount) {
		return String.format("%.2f %s", amount, TravelConfig.CURRENCY);
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		return value.trim();
	}

	public String getBookingId() {
		return bookingId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public LocalDate getTravelDate() {
		return travelDate;
	}

	public String getDestinationCity() {
		return destinationCity;
	}

	@Override
	public String toString() {
		return summary();
	}
}
