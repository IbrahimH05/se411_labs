package edu.spu.se411.lab07_polymorphism.model;

import java.time.LocalDate;

import edu.spu.se411.lab07_polymorphism.config.TravelConfig;
import edu.spu.se411.lab07_polymorphism.exceptions.InvalidArgumentException;
import edu.spu.se411.lab07_polymorphism.exceptions.MissingInformationException;

public class TrainBooking extends Booking {

	private final SeatClass seatClass;

	private Double distanceKm;

	public TrainBooking(String bookingId, String customerName, LocalDate travelDate, String destinationCity,
			SeatClass seatClass) {
		super(bookingId, customerName, travelDate, destinationCity);

		if (seatClass == null) {
			throw new IllegalArgumentException("Seat class is required");
		}
		this.seatClass = seatClass;
	}

	public void setDistanceKm(Double distanceKm) throws InvalidArgumentException {
		if (distanceKm != null) {
			requireInRange(distanceKm, TravelConfig.MIN_TRAIN_DISTANCE, TravelConfig.MAX_TRAIN_DISTANCE, "Distance");
		}
		this.distanceKm = distanceKm;
	}

	@Override
	public double computeTotalPrice() throws MissingInformationException, InvalidArgumentException {
		requireProvided(distanceKm, "Distance");
		requireInRange(distanceKm, TravelConfig.MIN_TRAIN_DISTANCE, TravelConfig.MAX_TRAIN_DISTANCE, "Distance");

		// The rate lives on the enum, so adding a seat class needs no change here.
		return distanceKm * seatClass.getRatePerKm();
	}

	@Override
	public String getBookingType() {
		return "Train";
	}

	@Override
	public String describeDetails() {
		return String.format("%s at %.2f/km, distance %s",
				seatClass, seatClass.getRatePerKm(),
				distanceKm == null ? "not provided" : distanceKm + " km");
	}

	public SeatClass getSeatClass() {
		return seatClass;
	}

	public Double getDistanceKm() {
		return distanceKm;
	}
}
