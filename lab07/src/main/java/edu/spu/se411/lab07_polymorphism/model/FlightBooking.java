package edu.spu.se411.lab07_polymorphism.model;

import java.time.LocalDate;

import edu.spu.se411.lab07_polymorphism.config.TravelConfig;
import edu.spu.se411.lab07_polymorphism.exceptions.InvalidArgumentException;
import edu.spu.se411.lab07_polymorphism.exceptions.MissingInformationException;

public class FlightBooking extends Booking {

	private final double basePrice;

	// Null until the customer declares it, which is what separates "missing" from "invalid".
	private Double luggageWeightKg;

	public FlightBooking(String bookingId, String customerName, LocalDate travelDate, String destinationCity,
			double basePrice) {
		super(bookingId, customerName, travelDate, destinationCity);

		if (basePrice < 0) {
			throw new IllegalArgumentException("Base ticket price cannot be negative: " + basePrice);
		}
		this.basePrice = basePrice;
	}

	public void setLuggageWeightKg(Double luggageWeightKg) throws InvalidArgumentException {
		if (luggageWeightKg != null) {
			requireInRange(luggageWeightKg, TravelConfig.MIN_LUGGAGE_WEIGHT, TravelConfig.MAX_LUGGAGE_WEIGHT,
					"Luggage weight");
		}
		this.luggageWeightKg = luggageWeightKg;
	}

	@Override
	public double computeTotalPrice() throws MissingInformationException, InvalidArgumentException {
		requireProvided(luggageWeightKg, "Luggage weight");
		requireInRange(luggageWeightKg, TravelConfig.MIN_LUGGAGE_WEIGHT, TravelConfig.MAX_LUGGAGE_WEIGHT,
				"Luggage weight");

		return basePrice + (luggageWeightKg * TravelConfig.EXTRA_LUGGAGE_RATE);
	}

	@Override
	public String getBookingType() {
		return "Flight";
	}

	@Override
	public String describeDetails() {
		return String.format("base %s, luggage %s",
				formatMoney(basePrice),
				luggageWeightKg == null ? "not provided" : luggageWeightKg + " kg");
	}

	public double getBasePrice() {
		return basePrice;
	}

	public Double getLuggageWeightKg() {
		return luggageWeightKg;
	}
}
