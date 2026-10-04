package edu.spu.se411.lab08.observers;

import edu.spu.se411.lab08.sensor.Sensor;

public class DashboardObserver extends SensorObserver {

	public DashboardObserver(String name) {
		super(name);
	}

	@Override
	protected void onReadingChanged(Sensor sensor) {
		System.out.printf("  [%-9s] %-14s %8.2f %-3s %s%n",
				getName(), sensor.getName(), sensor.getReading(), sensor.getUnit(), sensor.getStatus());
	}
}
