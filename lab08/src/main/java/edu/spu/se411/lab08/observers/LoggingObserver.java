package edu.spu.se411.lab08.observers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.spu.se411.lab08.sensor.Sensor;

public class LoggingObserver extends SensorObserver {

	private static final Logger logger = LoggerFactory.getLogger(LoggingObserver.class);

	public LoggingObserver(String name) {
		super(name);
	}

	@Override
	protected void onReadingChanged(Sensor sensor) {
		System.out.printf("  [%-9s] recorded %s%n", getName(), sensor.describe());

		if (!"COMFORTABLE".equals(sensor.getStatus())) {
			logger.warn("{} is {} at {} {}", sensor.getName(), sensor.getStatus(),
					String.format("%.2f", sensor.getReading()), sensor.getUnit());
		} else {
			logger.info("{} reading changed to {} {}", sensor.getName(),
					String.format("%.2f", sensor.getReading()), sensor.getUnit());
		}
	}
}
