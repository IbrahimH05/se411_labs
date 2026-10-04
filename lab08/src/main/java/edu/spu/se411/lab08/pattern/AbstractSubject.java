package edu.spu.se411.lab08.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSubject implements Subject, Cloneable {

	private List<Observer> observers = new ArrayList<>();

	@Override
	public void register(Observer o) {
		if (o == null) {
			throw new IllegalArgumentException("Observer is required");
		}
		if (!observers.contains(o)) {
			observers.add(o);
		}
	}

	@Override
	public void unregister(Observer o) {
		observers.remove(o);
	}

	@Override
	public void notifyObservers() {
		// Iterate a copy so an observer may unregister itself while being notified.
		for (Observer observer : new ArrayList<>(observers)) {
			observer.update(this);
		}
	}

	public int getObserverCount() {
		return observers.size();
	}

	public List<Observer> getObservers() {
		return Collections.unmodifiableList(observers);
	}

	/**
	 * Object.clone() is a shallow copy, so the clone would otherwise share the very
	 * same observer list as the original. The clone is given a fresh empty list: it
	 * starts with no observers and gathers its own.
	 */
	@Override
	protected AbstractSubject clone() throws CloneNotSupportedException {
		AbstractSubject copy = (AbstractSubject) super.clone();
		copy.observers = new ArrayList<>();
		return copy;
	}
}
