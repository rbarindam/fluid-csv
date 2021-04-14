package com.fluidapi.csv.reader.provider.deserializer;

/**
 * safely picks the string value at given index, and returns whatever is there.
 * 
 * @author Arindam Biswas
 * @since 1.0
 */
public class PickString extends PickMapped<String> {

	public PickString(int index) {
		super(index, t -> t);
	}

}
