package edu.spu.se411.lab07_polymorphism.model;

import java.time.LocalDate;

import edu.spu.se411.lab07_polymorphism.config.TravelConfig;
import edu.spu.se411.lab07_polymorphism.exceptions.InvalidArgumentException;
import edu.spu.se411.lab07_polymorphism.exceptions.MissingInformationException;

public class CarRentalBooking extends Booking {

	private final double dailyRate;

	private Integer rentalDays;

	public CarRentalBooking(String bookingId, String customerName, LocalDate travelDate, String destinationCity,
			double dailyRate) {
		super(bookingId, customerName, travelDate, destinationCity);

		if (dailyRate < 0) {
			throw new IllegalArgumentException("Daily rental rate cannot be negative: " + dailyRate);
		}
		this.dailyRate = dailyRate;
	}

	public void setRentalDays(Integer rentalDays) throws InvalidArgumentException {
		if (rentalDays != null) {
			requireInRange(rentalDays, TravelConfig.MIN_RENTAL_DAYS, TravelConfig.MAX_RENTAL_DAYS, "Rental days");
		}
		this.rentalDays = rentalDays;
	}

	@Override
	public double computeTotalPrice() throws MissingInformationException, InvalidArgumentException {
		requireProvided(rentalDays, "Rental days");
		requireInRange(rentalDays, TravelConfig.MIN_RENTAL_DAYS, TravelConfig.MAX_RENTAL_DAYS, "Rental days");

		return dailyRate * rentalDays;
	}

	@Override
	public String getBookingType() {
		return "Car rental";
	}

	@Override
	public String describeDetails() {
		return String.format("%s/day, %s",
				formatMoney(dailyRate),
				rentalDays == null ? "days not provided" : rentalDays + " days");
	}

	public double getDailyRate() {
		return dailyRate;
	}

	public Integer getRentalDays() {
		return rentalDays;
	}
}
