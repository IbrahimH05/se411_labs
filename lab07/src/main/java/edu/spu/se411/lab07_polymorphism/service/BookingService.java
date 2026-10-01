package edu.spu.se411.lab07_polymorphism.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.spu.se411.lab07_polymorphism.exceptions.BookingException;
import edu.spu.se411.lab07_polymorphism.model.Booking;

public class BookingService {

	private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

	// The polymorphism requirement: one method, any Booking subtype.
	public double computeTotalPrice(Booking booking) throws BookingException {
		if (booking == null) {
			throw new IllegalArgumentException("Booking is required");
		}

		double total = booking.computeTotalPrice();
		logger.debug("Computed total for {} ({}): {}", booking.getBookingId(), booking.getBookingType(), total);
		return total;
	}

	public double computeTotalRevenue(List<Booking> bookings) {
		double revenue = 0;

		for (Booking booking : bookings) {
			try {
				revenue += computeTotalPrice(booking);
			} catch (BookingException e) {
				logger.warn("Skipping {} in revenue total: {}", booking.getBookingId(), e.getMessage());
			}
		}

		return revenue;
	}

	public List<Booking> findPriceable(List<Booking> bookings) {
		List<Booking> priceable = new ArrayList<>();

		for (Booking booking : bookings) {
			try {
				computeTotalPrice(booking);
				priceable.add(booking);
			} catch (BookingException e) {
				logger.debug("{} is not priceable yet: {}", booking.getBookingId(), e.getMessage());
			}
		}

		return priceable;
	}
}
