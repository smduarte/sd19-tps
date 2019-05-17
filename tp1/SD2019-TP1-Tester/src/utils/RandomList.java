package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class RandomList<T> extends ArrayList<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final Random rg;

	public RandomList(Random rg) {
		this.rg = rg;
	}

	public RandomList(Random rg, Collection<? extends T> c) {
		super(c);
		this.rg = rg;
	}

	public T randomElement() {
		return isEmpty() ? null : get(rg.nextInt(super.size()));
	}

	public T removeRandomElement() {
		return isEmpty() ? null : remove(rg.nextInt(super.size()));
	}
}
