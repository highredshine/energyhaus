package Indy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.YearMonth;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *  This is Data class. Methods in this class can be said as mainly 3 domains.
 *  One is parsing the original data into various formats: hour, day, week, month, and season.
 *  Second is categorizing this data into multi-dimensioonal array, divided by years,
 *  meters, and time-frames parsed above.
 *  Third is searching methods, useful for the Graph class to find which data to present
 *  for the graphs.
 */
public class Data {

	private BufferedReader _reader;
	private ArrayList<Datum> _data;
	private ArrayList<Datum>[] _datasets;
	private Numbers[][][] _all;
	private int _weekNumber;
	
	/*
	 * Constructor: warning for instantiating a generic array of ArrayList was suppressed
	 * This was necessary to collect all datasets into one data structure; 
	 * handling them was more efficient this way.
	 */
	@SuppressWarnings("unchecked")
	public Data(){
		//Data Load
		_data = new ArrayList<Datum>();
		_datasets = new ArrayList[5];
		for (int i = 0; i < 5; i++) {
			_datasets[i] = new ArrayList<Datum>();
		}
		_all = new Numbers[4][5][5];
	}
	
	/*
	 * Data Parsing Methods
	 */
	
	/*
	 * Initial load
	 */
	public void loadData(String filename) {
		// Load up the reader first
		try {
			_reader = new BufferedReader(new FileReader(filename));
			String line = _reader.readLine();
			line = _reader.readLine();
			while (line != null) {
				String[] split = line.split(";");
				//Make datetime format
				String[] date = split[0].split("/");
				String key = String.join("-", date[2],date[1],date[0]) + " " + split[1];
				Double[] num = this.modify(split);
				Numbers values = new Numbers();
				values.setData(num);
				_data.add(new Datum(key,values));
				line =  _reader.readLine();
			}
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found!");
		} catch (IOException e) {
			System.out.println("Input/Output Invalid!");
		}
		this.fill();
	}
	
	//Hourly
	public void hourly() {
		int i = 0;
		while (i < _data.size()) {
			String hour = _data.get(i).getKey().split(":")[0];
			Double[] num = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			int j = i;
			while (_data.get(j).getKey().split(":")[0].equals(hour)) {
				j = this.parseHelper2(num, _data, j);
				if(this.parseHelper3(_data, j)) {
					break;
				}
			}
			this.parseHelper4(hour, num, j-i, 0);
			i = j;
		}
		// Remove imperfect values
		_datasets[0].remove(0);
		_datasets[0].remove(_datasets[0].size()-1);
	}
	
	//Daily
	public void daily() {
		int i = 0;
		while (i < _data.size()) {
			String day = _data.get(i).getKey().split(" ")[0];
			Double[] num = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			int j = i;
			while (_data.get(j).getKey().split(" ")[0].equals(day)) {
				j = this.parseHelper2(num, _data, j);
				if(this.parseHelper3(_data, j)) {
					break;
				}
			}
			this.parseHelper4(day, num, j-i, 1);
			i = j;
		}
	}
	
	//Weekly
	public void weekly() {
		//Data starts at 16/12/2006, which is Saturday. We skip this day and start from 17th.
		int i = 1;
		_weekNumber = 50;
		while (i < _datasets[1].size()) {
			String startDay = _datasets[1].get(i).getKey();
			String endDay = "";
			if (i+6 < _datasets[1].size()) {
				endDay += _datasets[1].get(i+6).getKey();
			}
			String week = startDay + "~" + endDay + " " + this.weekNumber(startDay, endDay);
			Double[] num = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			for (int j = i; j < i+7; j++) {
				if (j == _datasets[1].size()) {
					break;
				}
				this.parseHelper1(num, _datasets[1], j);
			}
			this.parseHelper4(week, num, 7, 2);
			i += 7;
		}
		// Last item is not full week, so delete
		_datasets[2].remove(_datasets[2].size()-1);
	}
	
	public String weekNumber(String startDay, String endDay) {
		String[] start = startDay.split("-");
		if (!start[0].equals(endDay.split("-")[0])) {
			_weekNumber = 1;
		} else {
			_weekNumber += 1;
		}
		return "Week "+ String.valueOf(_weekNumber);
	}
	
	//Monthly
	public void monthly() {
		int i = 0;
		while (i < _datasets[1].size()) {
			String[] date = _datasets[1].get(i).getKey().split("-");
			String month =  date[0] + "-" + date[1];
			Double[] num = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			int j = i;
			while (_datasets[1].get(j).getKey().split("-")[1].equals(date[1])) {
				j = this.parseHelper2(num, _datasets[1], j);
				if(this.parseHelper3(_datasets[1], j)) {
					break;
				}
			}
			this.parseHelper4(month, num, j-i, 3);
			i = j;
		}
		// Take off non-full months
		_datasets[3].remove(0);
		_datasets[3].remove(_datasets[3].size()-1);
	}
	
	//Seasonal
	public void seasonal() {
		int i = 2;
		// Season modulo: start from Spring, so 1.
		int j = 1;
		while (i < _datasets[3].size()-2) {
			String season =  _datasets[3].get(i).getKey().split("-")[0] + "-" + Constants.SEASONS[j%4];
			Double[] num = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			for (int k = i; k < i+3; k++) {
				this.parseHelper1(num, _datasets[3], k);
			}
			this.parseHelper4(season, num, 3, 4);
			j += 1;
			i += 3;
		}
	}
	
	/*
	 * Parsing Helper Methods
	 */
	
	public void parseHelper1(Double[] num, ArrayList<Datum> data, int j) {
		// Sum up power consumptions (unit: kWH)
		for (int k = 0; k < num.length; k++) {
			num[k] += data.get(j).getValue().get(Constants.KEYS[k]);
		}
	}
	
	public int parseHelper2(Double[] num, ArrayList<Datum> data, int j) {
		this.parseHelper1(num, data, j);
		j += 1;
		return j;
	}
	
	public boolean parseHelper3(ArrayList<Datum> data, int j) {
		boolean parse = false;
		if (j == data.size()) {
			parse = true;
		}
		return parse;
	}
	
	public void parseHelper4(String key, Double[] num, int avg, int type) {
		// Average Voltage and Intensity (units: V, A)
		num[2] /= avg;
		num[3] /= avg;
		for (int k = 0; k < num.length; k++) {
			num[k] = Math.round(num[k] * 100D) / 100D;
		}
		Numbers values = new Numbers();
		values.setData(num);
		_datasets[type].add(new Datum(key,values));
	}
	
	/*
	 * Another helper methods to modify and fill abnormal values
	 *  in the original text file
	 */
	
	/*
	 * modify non-numeric values into numbers
	 */
	public Double[] modify(String[] current) {
		Double[] numbers = new Double[8];
		for (int i = 2; i < 9; i++) {
			double value;
			try {
				value = Double.valueOf(current[i]);
			} catch(NullPointerException e) {
				value = -1;
			} catch(NumberFormatException e) {
				value = -1;
			} catch(ArrayIndexOutOfBoundsException e) {
				value = -1;
			}
			numbers[i-2] = value;
		}
		return numbers;
	}
	
	//fill missing values
	public void fill() {
		for (int i = 0; i < _data.size(); i++) {
			for (int j = 0; j < 7; j++) {
				// For null spaces, use the previous day's value at the time
				if (_data.get(i).getValue().get(Constants.KEYS[j]) < 0) {
					_data.get(i).getValue().replace(Constants.KEYS[j], 
							_data.get(i-Constants.DAY).getValue().get(Constants.KEYS[j]));
				}
			}
			// Calculate sub metering for the remainder of the house
			double sub4 = _data.get(i).getValue().get("Active_power") * 1000 / 60 - 
					(_data.get(i).getValue().get("Sub_meter_1") + 
							_data.get(i).getValue().get("Sub_meter_2") + 
							_data.get(i).getValue().get("Sub_meter_3"));
			_data.get(i).getValue().replace("Sub_meter_4", (Math.round(sub4) * 100D) / 100D);
		}
	}
	
	/*
	 * Categorization methods
	 */
	
	/*
	 * Cutting each dataset into years from 2007 to 2010.
	 */
	public ArrayList<Datum> cutByYear(ArrayList<Datum> data, int year) {
		int i = 0;
		int checker = 0;
		while (i < data.size()) {
			// Current datum's year value
			checker = Integer.valueOf(data.get(i).getKey().split("-")[0]);
			if (checker == year) {
				break;
			}
			i += 1;
		}
		int j = i;
		while (checker == year) {
			j += 1;
			if (j == data.size()) {
				break;
			}
			checker = Integer.valueOf(data.get(j).getKey().split("-")[0]);
		}
		ArrayList<Datum> cut = new ArrayList<Datum>(data.subList(i,j));
		// edge case: 2006-12-31
		if (cut.size() > 0 && cut.get(0).getKey().equals("2007-1-1")) {
			cut.add(0, data.get(15));
		}
		return cut;
	}
	
	/*
	 * Cutting each cut year data for different meters, from Total to all four sub-meters
	 */
	public Numbers cutByCategory(ArrayList<Datum> data, String key) {
		Numbers nums = new Numbers();
		for (int i = 0; i < data.size(); i++) {
			nums.put(data.get(i).getKey(), data.get(i).getValue().get(key));
		}
		return nums;
	}
	
	/*
	 * Searching method that returns an appropriate data for certain inputs of date and datatypes
	 */
	public Numbers search(Numbers data, String date, int type, int dataType) {
		int i = this.findIndex(data, date, type);
		int[] dates = Arrays.stream(date.split("-")).mapToInt(Integer::parseInt).toArray(); 
		int j = i;
		switch(type) {
		case 0: case 1: case 2:
			switch(type) {
			case 0: //day: 24 hours
				j += 24;
				break;
			case 1: //week: 7 days
				j += 7;
				break;
			case 2: //month: 31 days
				j += YearMonth.of(Integer.valueOf(dates[0]), 
						Integer.valueOf(dates[1])).lengthOfMonth();
				break;
			default:
				break;
			}
			if (j >= data.size()) {
				j = data.size();
			}
			return data.subDictionary(i, j);
		case 3: //season: 3 months
			switch (dates[1]) {
			case 1: case 2: case 12:
				Numbers searched = new Numbers();
				if (dates[1] == 12) {
					// Put Dec
					searched.put(data.keyArray()[data.size()-1], data.valueArray()[data.size()-1]);
					Numbers next = _all[dates[0]+1-2007][type][dataType];
					searched.put(next.keyArray()[0], next.valueArray()[0]);
					searched.put(next.keyArray()[1], next.valueArray()[1]);
				} else {
					// Put Jan and Feb
					searched.put(data.keyArray()[0], data.valueArray()[0]);
					searched.put(data.keyArray()[1], data.valueArray()[1]);
					// take in last month of prev year
					Numbers prev = _all[dates[0]-1-2007][type][dataType];
					searched.put(prev.keyArray()[prev.size()-1], prev.valueArray()[prev.size()-1]);
				}
				return searched;
			default:
				j += 3;
				if (j >= data.size()) {
					j = data.size();
				}
				return data.subDictionary(i, j);
			}
		default:
			return null;
		}
	}
	
	/*
	 * Helper method for searching: finds the index of an input on the total data
	 */
	public int findIndex(Numbers data, String target, int type) {
		int i = 0;
		String checker = "";
		while (i < data.size()) {
			// Current datum's year value
			switch(type) {
			case 0: //day: 24 hours; from "yyyy-mm-dd hour" to "yyyy-mm-dd"
				checker = data.keyArray()[i].split(" ")[0];
				break;
			case 1: 
				checker = data.keyArray()[i];
				break;
			case 2: case 3: // week: 7 days; input: "yyyy-mm-dd~yyyy-mm-dd"
				String[] date = data.keyArray()[i].split("-");
				checker = date[0]+"-"+date[1];
				break;
			default:
				break;
			}
			if (checker.equals(target)) {
				break;
			}
			i += 1;
		}
		return i;
	}

	/*
	 * Getter methods
	 */
	
	public ArrayList<Datum> getData() {
		return _data;
	}
	
	public ArrayList<Datum>[] getDatasets() {
		return _datasets;
	}
	
	public Numbers[][][] getAll() {
		return _all;
	}
	
	/*
	 * Datum class models each datum of the whole data.
	 * One datum class is an entry of a large dictionary set.
	 * Declared public as it is used in other classes as well
	 */
	public class Datum extends AbstractMap.SimpleEntry<String,Numbers> {

		/**
		 * This is to suppress warnings on generic instantiations
		 */
		private static final long serialVersionUID = 1L;

		public Datum(String key, Numbers value) {
			super(key, value);
		}
	}
}
