package edu.spu.se411.lab08;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.spu.se411.lab08.observers.DashboardObserver;
import edu.spu.se411.lab08.observers.LoggingObserver;
import edu.spu.se411.lab08.sensor.HumiditySensor;
import edu.spu.se411.lab08.sensor.TemperatureSensor;

public class App {

	private static final Logger logger = LoggerFactory.getLogger(App.class);

	private static final int READINGS = 10;

	// The handout's loop sleeps 1000 ms; override with -Dreading.interval.ms=0 to run fast.
	private static final long INTERVAL_MS = Long.getLong("reading.interval.ms", 1000L);

	public static void main(String[] args) {
		logger.info("Application is starting...");

		TemperatureSensor temp = new TemperatureSensor("Temperature", 22.0);
		HumiditySensor humidity = new HumiditySensor("Humidity", 50.0);

		DashboardObserver dashboard = new DashboardObserver("Dashboard");
		LoggingObserver auditLog = new LoggingObserver("Logger");

		temp.register(dashboard);
		temp.register(auditLog);
		humidity.register(dashboard);
		humidity.register(auditLog);

		logger.info("Registered {} observers on {} and {} on {}",
				temp.getObserverCount(), temp.getName(), humidity.getObserverCount(), humidity.getName());

		simulateReadings(temp, humidity);
		demonstrateUnregister(temp, dashboard, auditLog);
		demonstrateCloning(temp, dashboard, auditLog);

		System.out.printf("%nDashboard received %d updates, Logger received %d.%n",
				dashboard.getUpdateCount(), auditLog.getUpdateCount());

		logger.info("Application is stopping...");
	}

	private static void simulateReadings(TemperatureSensor temp, HumiditySensor humidity) {
		heading("Simulating " + READINGS + " sensor readings");

		Random random = new Random();

		for (int i = 0; i < READINGS; i++) {
			System.out.printf("%nreading %d%n", i + 1);

			temp.setReading(20 + random.nextDouble() * 15);
			humidity.setReading(40 + random.nextDouble() * 20);

			try {
				Thread.sleep(INTERVAL_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("Simulation interrupted", e);
				return;
			}
		}
	}

	private static void demonstrateUnregister(TemperatureSensor temp,
			DashboardObserver dashboard, LoggingObserver auditLog) {

		heading("Unregistering the dashboard");

		temp.unregister(dashboard);
		System.out.printf("  %s now has %d observer(s); only the logger should answer%n",
				temp.getName(), temp.getObserverCount());
		temp.setReading(31.5);

		temp.register(dashboard);
	}

	private static void demonstrateCloning(TemperatureSensor original,
			DashboardObserver dashboard, LoggingObserver auditLog) {

		heading("Cloning the sensor");

		TemperatureSensor copy = original.clone();

		System.out.printf("  original : %s, %d observer(s)%n", original.describe(), original.getObserverCount());
		System.out.printf("  clone    : %s, %d observer(s)%n", copy.describe(), copy.getObserverCount());
		System.out.printf("  clone is a %s and is a distinct object: %b%n",
				copy.getClass().getSimpleName(), original != copy);

		System.out.println("\n  changing the clone - the original's observers must stay silent:");
		copy.setReading(40.0);
		System.out.println("  (nothing above means the observer lists are genuinely separate)");

		System.out.println("\n  giving the clone its own observer:");
		DashboardObserver cloneDashboard = new DashboardObserver("Clone-DB");
		copy.register(cloneDashboard);
		copy.setReading(12.0);

		System.out.printf("%n  original still has %d observer(s), clone has %d%n",
				original.getObserverCount(), copy.getObserverCount());

		logger.info("Cloned {}: original has {} observers, clone has {}",
				original.getName(), original.getObserverCount(), copy.getObserverCount());
	}

	private static void heading(String title) {
		System.out.println();
		System.out.println("== " + title + " ==");
	}
}
