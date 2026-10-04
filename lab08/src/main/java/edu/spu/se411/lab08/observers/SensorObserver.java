package edu.spu.se411.lab08.observers;

import edu.spu.se411.lab08.pattern.Observer;
import edu.spu.se411.lab08.pattern.Subject;
import edu.spu.se411.lab08.sensor.Sensor;

public abstract class SensorObserver implements Observer {

	private final String name;

	private int updateCount;

	protected SensorObserver(String name) {
		this.name = name;
	}

	/**
	 * The interface hands over a Subject, so every sensor observer would otherwise
	 * repeat the same narrowing. It is done once here; subclasses receive a Sensor.
	 */
	@Override
	public final void update(Subject subject) {
		if (subject instanceof Sensor sensor) {
			updateCount++;
			onReadingChanged(sensor);
		}
	}

	protected abstract void onReadingChanged(Sensor sensor);

	public String getName() {
		return name;
	}

	public int getUpdateCount() {
		return updateCount;
	}
}
