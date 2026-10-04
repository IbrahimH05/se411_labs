package edu.spu.se411.lab08.sensor;

public class TemperatureSensor extends Sensor {

	public static final double COLD_BELOW = 18.0;
	public static final double HOT_ABOVE = 28.0;

	public TemperatureSensor(String name, double initialReading) {
		super(name, initialReading);
	}

	@Override
	public String getUnit() {
		return "°C";
	}

	@Override
	public String getStatus() {
		double reading = getReading();

		if (reading < COLD_BELOW) {
			return "COLD";
		}
		if (reading > HOT_ABOVE) {
			return "HOT";
		}
		return "COMFORTABLE";
	}

	@Override
	public TemperatureSensor clone() {
		return (TemperatureSensor) super.clone();
	}
}
