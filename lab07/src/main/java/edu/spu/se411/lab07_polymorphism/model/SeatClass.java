package edu.spu.se411.lab07_polymorphism.model;

import edu.spu.se411.lab07_polymorphism.config.TravelConfig;

public enum SeatClass {

	STANDARD(TravelConfig.TRAIN_STANDARD_RATE),
	FIRST_CLASS(TravelConfig.TRAIN_FIRST_CLASS_RATE);

	private final double ratePerKm;

	SeatClass(double ratePerKm) {
		this.ratePerKm = ratePerKm;
	}

	public double getRatePerKm() {
		return ratePerKm;
	}
}
