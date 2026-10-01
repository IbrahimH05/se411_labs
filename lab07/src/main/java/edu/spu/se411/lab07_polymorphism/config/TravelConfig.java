package edu.spu.se411.lab07_polymorphism.config;

public final class TravelConfig {

	public static final double EXTRA_LUGGAGE_RATE = 12.50;

	public static final double TRAIN_STANDARD_RATE = 0.35;
	public static final double TRAIN_FIRST_CLASS_RATE = 0.60;

	public static final double MIN_LUGGAGE_WEIGHT = 0.0;
	public static final double MAX_LUGGAGE_WEIGHT = 40.0;

	public static final double MIN_TRAIN_DISTANCE = 1.0;
	public static final double MAX_TRAIN_DISTANCE = 2000.0;

	public static final int MIN_RENTAL_DAYS = 1;
	public static final int MAX_RENTAL_DAYS = 30;

	public static final String CURRENCY = "USD";

	private TravelConfig() {
		throw new AssertionError("TravelConfig is a constant holder and must not be instantiated");
	}
}
