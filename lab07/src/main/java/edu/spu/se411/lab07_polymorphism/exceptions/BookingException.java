package edu.spu.se411.lab07_polymorphism.exceptions;

public abstract class BookingException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String bookingId;
	private final String fieldName;

	protected BookingException(String message, String bookingId, String fieldName) {
		super(message);
		this.bookingId = bookingId;
		this.fieldName = fieldName;
	}

	public String getBookingId() {
		return bookingId;
	}

	public String getFieldName() {
		return fieldName;
	}
}
