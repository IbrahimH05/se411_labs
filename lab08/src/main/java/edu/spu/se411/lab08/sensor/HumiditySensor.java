package edu.spu.se411.lab08.sensor;

public class HumiditySensor extends Sensor {

	public static final double DRY_BELOW = 40.0;
	public static final double HUMID_ABOVE = 60.0;

	public HumiditySensor(String name, double initialReading) {
		super(name, initialReading);
	}

	@Override
	public String getUnit() {
		return "%";
	}

	@Override
	public String getStatus() {
		double reading = getReading();

		if (reading < DRY_BELOW) {
			return "DRY";
		}
		if (reading > HUMID_ABOVE) {
			return "HUMID";
		}
		return "COMFORTABLE";
	}

	@Override
	public HumiditySensor clone() {
		return (HumiditySensor) super.clone();
	}
}
