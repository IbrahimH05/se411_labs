package edu.spu.se411.lab07_polymorphism.exceptions;

public class InvalidArgumentException extends BookingException {

	private static final long serialVersionUID = 1L;

	private final double value;
	private final double min;
	private final double max;

	public InvalidArgumentException(String bookingId, String fieldName, double value, double min, double max) {
		super("Booking " + bookingId + ": " + fieldName + " is " + value
				+ ", which is outside the allowed range " + min + " to " + max, bookingId, fieldName);
		this.value = value;
		this.min = min;
		this.max = max;
	}

	public double getValue() {
		return value;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}
}
