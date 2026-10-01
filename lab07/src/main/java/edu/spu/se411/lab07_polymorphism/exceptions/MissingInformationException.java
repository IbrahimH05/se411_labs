package edu.spu.se411.lab07_polymorphism.exceptions;

public class MissingInformationException extends BookingException {

	private static final long serialVersionUID = 1L;

	public MissingInformationException(String bookingId, String fieldName) {
		super("Booking " + bookingId + ": " + fieldName + " has not been provided yet", bookingId, fieldName);
	}
}
