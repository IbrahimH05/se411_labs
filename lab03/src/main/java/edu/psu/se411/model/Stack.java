package edu.psu.se411.model;

import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Class Under Unit Test.
 */
public class Stack<E> {

	private final ArrayList<E> elements;
	
	public Stack() {
		this(10);
	}
	
	public Stack(int capacity) {
		int initCapacity = capacity > 0 ? capacity : 10;
		elements = new ArrayList<E>(initCapacity);
	}
	
	public void push(E pushValue) {
		elements.add(pushValue);
	}
	
	public E pop() {
		if(elements.isEmpty()) {
			throw new NoSuchElementException("Stack is empty, cannot pop");
		}
		
		return elements.remove(elements.size() - 1);
	}
	
	
	
}
