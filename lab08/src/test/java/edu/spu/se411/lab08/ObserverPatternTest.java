package edu.spu.se411.lab08;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.spu.se411.lab08.observers.SensorObserver;
import edu.spu.se411.lab08.pattern.Observer;
import edu.spu.se411.lab08.pattern.Subject;
import edu.spu.se411.lab08.sensor.HumiditySensor;
import edu.spu.se411.lab08.sensor.Sensor;
import edu.spu.se411.lab08.sensor.TemperatureSensor;

public class ObserverPatternTest {

	/** Records what it was told, so tests can assert on notifications. */
	static class RecordingObserver extends SensorObserver {

		final List<Double> readings = new ArrayList<>();

		RecordingObserver(String name) {
			super(name);
		}

		@Override
		protected void onReadingChanged(Sensor sensor) {
			readings.add(sensor.getReading());
		}
	}

	private TemperatureSensor temperature;
	private RecordingObserver first;
	private RecordingObserver second;

	@BeforeEach
	public void setUp() {
		temperature = new TemperatureSensor("Temperature", 22.0);
		first = new RecordingObserver("first");
		second = new RecordingObserver("second");
	}

	// Registration and notification
	@Test
	public void every_registered_observer_is_notified() {
		temperature.register(first);
		temperature.register(second);

		temperature.setReading(30.0);

		assertEquals(List.of(30.0), first.readings);
		assertEquals(List.of(30.0), second.readings);
	}

	@Test
	public void an_unregistered_observer_stops_being_notified() {
		temperature.register(first);
		temperature.register(second);

		temperature.setReading(25.0);
		temperature.unregister(first);
		temperature.setReading(26.0);

		assertEquals(List.of(25.0), first.readings);
		assertEquals(List.of(25.0, 26.0), second.readings);
	}

	@Test
	public void registering_the_same_observer_twice_notifies_it_once() {
		temperature.register(first);
		temperature.register(first);

		temperature.setReading(27.0);

		assertEquals(1, temperature.getObserverCount());
		assertEquals(1, first.readings.size());
	}

	@Test
	public void a_null_observer_is_rejected() {
		assertThrows(IllegalArgumentException.class, () -> temperature.register(null));
	}

	@Test
	public void unregistering_an_observer_that_was_never_registered_is_harmless() {
		assertDoesNotThrow(() -> temperature.unregister(first));
	}

	@Test
	public void setting_the_same_value_again_notifies_nobody() {
		temperature.register(first);

		temperature.setReading(25.0);
		temperature.setReading(25.0);

		assertEquals(1, first.readings.size());
	}

	@Test
	public void an_observer_may_unregister_itself_while_being_notified() {
		Observer selfRemoving = new Observer() {
			@Override
			public void update(Subject subject) {
				subject.unregister(this);
			}
		};

		temperature.register(selfRemoving);
		temperature.register(first);

		// Iterating the live list here would throw ConcurrentModificationException.
		assertDoesNotThrow(() -> temperature.setReading(29.0));
		assertEquals(1, temperature.getObserverCount());
		assertEquals(1, first.readings.size());
	}

	// Cloning
	@Test
	public void a_clone_starts_with_no_observers() {
		temperature.register(first);
		temperature.register(second);

		TemperatureSensor copy = temperature.clone();

		assertEquals(2, temperature.getObserverCount());
		assertEquals(0, copy.getObserverCount());
	}

	@Test
	public void changing_the_clone_never_notifies_the_originals_observers() {
		temperature.register(first);

		TemperatureSensor copy = temperature.clone();
		copy.setReading(35.0);

		assertTrue(first.readings.isEmpty());
	}

	@Test
	public void changing_the_original_never_notifies_the_clones_observers() {
		TemperatureSensor copy = temperature.clone();
		copy.register(second);

		temperature.setReading(31.0);

		assertTrue(second.readings.isEmpty());
	}

	@Test
	public void the_two_observer_lists_are_genuinely_separate_objects() {
		TemperatureSensor copy = temperature.clone();

		copy.register(first);
		temperature.register(second);

		assertEquals(1, temperature.getObserverCount());
		assertEquals(1, copy.getObserverCount());
		assertFalse(temperature.getObservers().contains(first));
		assertFalse(copy.getObservers().contains(second));
	}

	@Test
	public void a_clone_copies_the_state_but_is_a_distinct_object() {
		temperature.setReading(26.5);

		TemperatureSensor copy = temperature.clone();

		assertNotSame(temperature, copy);
		assertEquals("Temperature", copy.getName());
		assertEquals(26.5, copy.getReading());
	}

	@Test
	public void a_clone_keeps_the_runtime_type_of_the_subclass() {
		HumiditySensor humidity = new HumiditySensor("Humidity", 50.0);

		assertInstanceOf(TemperatureSensor.class, temperature.clone());
		assertInstanceOf(HumiditySensor.class, humidity.clone());
		assertSame(TemperatureSensor.class, temperature.clone().getClass());
	}

	// Polymorphism
	@Test
	public void each_sensor_type_reports_its_own_unit_and_status() {
		HumiditySensor humidity = new HumiditySensor("Humidity", 50.0);

		assertEquals("°C", temperature.getUnit());
		assertEquals("%", humidity.getUnit());

		temperature.setReading(10.0);
		humidity.setReading(10.0);

		assertEquals("COLD", temperature.getStatus());
		assertEquals("DRY", humidity.getStatus());
	}

	@Test
	public void the_same_reading_means_different_things_to_different_sensors() {
		HumiditySensor humidity = new HumiditySensor("Humidity", 50.0);

		List<Sensor> sensors = List.of(temperature, humidity);
		for (Sensor sensor : sensors) {
			sensor.setReading(50.0);
		}

		assertEquals("HOT", temperature.getStatus());
		assertEquals("COMFORTABLE", humidity.getStatus());
	}

	@Test
	public void temperature_status_boundaries() {
		temperature.setReading(TemperatureSensor.COLD_BELOW);
		assertEquals("COMFORTABLE", temperature.getStatus());

		temperature.setReading(TemperatureSensor.COLD_BELOW - 0.01);
		assertEquals("COLD", temperature.getStatus());

		temperature.setReading(TemperatureSensor.HOT_ABOVE);
		assertEquals("COMFORTABLE", temperature.getStatus());

		temperature.setReading(TemperatureSensor.HOT_ABOVE + 0.01);
		assertEquals("HOT", temperature.getStatus());
	}

	// Shared base behaviour
	@Test
	public void a_sensor_observer_ignores_a_subject_that_is_not_a_sensor() {
		Subject notASensor = new edu.spu.se411.lab08.pattern.AbstractSubject() {
		};

		notASensor.register(first);
		notASensor.notifyObservers();

		assertTrue(first.readings.isEmpty());
		assertEquals(0, first.getUpdateCount());
	}

	@Test
	public void a_blank_sensor_name_is_rejected() {
		assertThrows(IllegalArgumentException.class, () -> new TemperatureSensor("  ", 20.0));
	}

	@Test
	public void the_observer_list_cannot_be_modified_from_outside() {
		temperature.register(first);
		assertThrows(UnsupportedOperationException.class, () -> temperature.getObservers().clear());
	}
}
