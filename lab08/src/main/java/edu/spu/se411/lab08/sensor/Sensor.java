package edu.spu.se411.lab08.sensor;

import edu.spu.se411.lab08.pattern.AbstractSubject;

public abstract class Sensor extends AbstractSubject {

	private final String name;
	private double reading;

	protected Sensor(String name, double initialReading) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Sensor name is required");
		}
		this.name = name;
		this.reading = initialReading;
	}

	public abstract String getUnit();

	public abstract String getStatus();

	/** Notifies only when the value actually moved, as the problem statement says. */
	public void setReading(double reading) {
		if (Double.compare(this.reading, reading) == 0) {
			return;
		}
		this.reading = reading;
		notifyObservers();
	}

	public double getReading() {
		return reading;
	}

	public String getName() {
		return name;
	}

	public String describe() {
		return String.format("%s = %.2f %s (%s)", name, reading, getUnit(), getStatus());
	}

	/**
	 * Narrows the return type and drops the checked exception, which an override is
	 * allowed to do. Sensor implements Cloneable, so the exception cannot occur.
	 */
	@Override
	public Sensor clone() {
		try {
			return (Sensor) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError("Sensor implements Cloneable, so this cannot happen", e);
		}
	}

	@Override
	public String toString() {
		return describe();
	}
}
