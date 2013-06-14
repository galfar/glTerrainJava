package com.galfarslair.util;

import java.util.ArrayList;
import java.util.List;

public abstract class SequenceExecutor<T extends Enum<T>> {

	protected T current;
	protected List<T> values = new ArrayList<T>();
	
	// Needs parameter to get generic enum values unfortunately.
	// http://stackoverflow.com/questions/2205891/iterate-enum-values-using-java-generics
	public SequenceExecutor(Class<T> enumClass) {		
        for (T value : enumClass.getEnumConstants()) {
        	values.add(value);
        }		
        assert values.size() > 0;
		current = values.get(0);
	}
	
	public boolean execute() {
		processState();
		
		if (current == values.get(values.size() - 1)) {
			return true;
		} else {
			current = values.get(values.indexOf(current) + 1);
			return false;
		}		
	}
	
	protected abstract void processState();
}
