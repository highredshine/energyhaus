package Indy;

import java.util.Arrays;
import java.util.LinkedHashMap;

/*
 * Numbers is a modified class used for this package,
 * extending from a sorted dictionary class.
 * The order of key-value pairs in the dictionary is maintained,
 * which is thus called sorted. 
 * This implementation was valuable to make arrays for keys and values
 * as the data is all based on time-series.
 */
@SuppressWarnings("serial")
public class Numbers extends LinkedHashMap<String,Double> {

	/*
	 * Different constructors for different situations.
	 * One is to call an empty dictionary and
	 * another is to create a full dictionary from the start
	 */
	public Numbers() {
		super();
	}
	
	public Numbers(String[] keys, Double[] values) {
		super();
		for (int i = 0; i < keys.length; i++) {
			this.put(keys[i], values[i]);
		}
	}
	
	/*
	 * data putting methods, for determined keys or any kind of keys
	 */
	
	public void setData(Double[] values) {
		for (int i = 0; i < 8; i++) {
			this.put(Constants.KEYS[i], values[i]);
		}
	}
	
	public void setData(String[] keys, Double[] values) {
		for (int i = 0; i < 8; i++) {
			this.put(keys[i], values[i]);
		}
	}
	
	/*
	 * array bulding methods for keys and values
	 */
	
	public String[] keyArray() {
		return this.keySet().toArray(new String[this.size()]);
	}
	
	public Double[] valueArray() {
		return this.values().toArray(new Double[this.size()]);
	}
	
	/*
	 * parsing methods for arrays and dictionaries
	 */
	
	public Double[] valueFromTo(int from, int to) {
		return Arrays.copyOfRange(this.valueArray(), from, to);
	}
	
	public Numbers subDictionary(int i, int j) {
		String[] xVal = Arrays.copyOfRange(this.keyArray(), i, j);
		Double[] yVal = Arrays.copyOfRange(this.valueArray(), i, j);
		return new Numbers(xVal, yVal);
	}
	
}