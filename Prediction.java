package Indy;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/*
 * This is Prediction class, the main star of this package!
 * The class is divided into methods for training the ML model,
 * generating average graphs to compare the predicted value,
 * and update methods for different inputs from the menu.
 */
public class Prediction {
	
	private Calculator _calc;
	private Pane _result;
	private MenuButton  _optionButton;
	private DatePicker _datePicker;
	private Data _data;
	private LocalDate _date;
	private DecimalFormat _df;
	private Numbers[] _average;
	private XYChart<String,Number>[] _chartHolder;
	private XYChart.Series<String, Number>[] _series;
	private int _p, _chartNum, _month, _week, _day, _season;
	private Double[] _train, _test;
	private Double[][] _all;
	private double[] _current;
	private double[][] _houseData, _coeff;
	private double[][][] _predictions;
	
	/*
	 * Constructor: warning suppressed for instantiation of array of generic classes
	 * such as XYChart or Series
	 */
	@SuppressWarnings("unchecked")
	public Prediction(VBox contentPane, Data data) {
		_data = data;
		_calc = new Calculator();
		_predictions = new double[5][4][]; //[dataType(house area)][type(day,week,month,year)][date]
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 4; j++) {
				_predictions[i][j] = new double[Constants.TYPE_NUM[j]];
			}
		}
		_p = 0;
		_average = new Numbers[4];
		_chartHolder = new XYChart[4];
		_houseData = new double[5][4];
		_coeff = new double[5][];
		_current = new double[4];
        _series = new XYChart.Series[4];
		_df = new DecimalFormat(".##");
		this.setup(contentPane);
	}
	
	/*
	 * split data for 2010 year into train and test data.
	 * Using the train and test the program will find the optimal
	 * ML autoregression model.
	 */
	public void splitData() {
		// Determine train and test datasets: We use 2010 year as train/test. Dec is test data.
		Numbers latest = _data.getAll()[3][1][0];
		int i = _data.findIndex(latest, "2010-11-1", 1);
		_train = latest.valueFromTo(0, i);
		_test = latest.valueFromTo(i, latest.size());
		// We store all numbers of day for each metering and total in here.
		_all = new Double[5][];
		for (int j = 0; j < 5; j++) {
			_all[j] = _data.getAll()[3][1][j].valueArray();
		}
	}
	
	/*
	 * calculate BIC (Bayesian Information Criteria) for test data,
	 * and finalize the optimal number of parameters to use
	 * for the autoregression equation. 
	 */
	public void setOptimalParam() {
		// Get BIC values for all test data
		ArrayList<double[]> infos = new ArrayList<double[]>();
		for (int i = 0; i < _test.length; i++) {
			ArrayList<Double> input = new ArrayList<Double>();
			input.addAll(Arrays.asList(_train));
			for (int k = 0; k < i; k++) {
				input.add(_test[k]);
			}
			double[] info = new double[Constants.MAX_PARAM];
			for (int j = 1; j < info.length; j++) {
				double[][] x = _calc.xMatrix(input, j);
				double[][] y = _calc.yVector(input, j);
				double[][] b = _calc.leastSquares(x,y);
				int n = input.size();
				double infoCriteria = _calc.infoCriteria(x,y,b,n);
				info[j] = infoCriteria;
			}
			infos.add(info);
		}
		// Finid minimum BIC
		int p = 0;
		double minBIC = 100;
		for (int i = 1; i < Constants.MAX_PARAM; i++) {
			double sum = 0;
			for (int j = 0; j < _test.length; j++) {
				sum += infos.get(j)[i];
			}
			double bic = sum/_test.length;
			if (minBIC > bic) {
				minBIC = bic;
				p = i;
			}
		}
		_p = p;
		// Find the regression equation coefficients with the optimal p value
		for (int i = 0; i < 5; i++) {
			ArrayList<Double> input = new ArrayList<Double>();
			input.addAll(Arrays.asList(_all[i]));
			double[][] x = _calc.xMatrix(input, _p);
			double[][] y = _calc.yVector(input, _p);
			double[][] b = _calc.leastSquares(x, y);
			_coeff[i] = new double[b.length];
			for (int j = 0; j < b.length; j++) {
				_coeff[i][j] = b[j][0];
			}
		}
	}
	
	/*
	 * Generate average graphs for all time frames from day to season.
	 * These graphs do not change so we store them in the holder
	 * and do not update them anymore.
	 */
	public void generateAverageGraphs() {
		this.averageData();
		for (int i = 0; i < Constants.TYPES.length; i++) {
			CategoryAxis xAxis = new CategoryAxis();
	        NumberAxis yAxis = new NumberAxis();
	        LineChart<String, Number> chart = new LineChart<String, Number>(xAxis, yAxis);
	        XYChart.Series<String,Number> series = new XYChart.Series<String, Number>();
	        series.setName(Constants.TYPES[i] + " Average from 2007 to 2010");
	        for (int j = 0; j < Constants.TYPE_NUM[i]; j++) {
	        	series.getData().add(new XYChart.Data<String, Number>(
	        			_average[i].keyArray()[j], (Number)_average[i].valueArray()[j]));
	        }
	        chart.getData().add(series);
	        chart.setMaxSize(Constants.ML_SIZE[0],Constants.ML_SIZE[1]);
	        chart.setCreateSymbols(false);
	        _chartHolder[i] = chart;
		}
		_chartNum = 1;
	}
	
	/*
	 * Helper method for average graph generation method above.
	 * Calculates average values for all time frames and piles
	 * into new data structure
	 */
	public void averageData() {
		for (int i = 0; i < 4; i++) {
			Numbers avgNum = new Numbers();
			for (int j = 0; j < Constants.TYPE_NUM[i]; j++) {
				String key = Constants.TYPES[i] + " " + String.valueOf(j+1);
				if (i == 3) {
					key = Constants.SEASONS[j];
				}
				double value = 0;
				double n = 0;
				for (int k = 0; k < 4; k++) {
					if (i == 3) {
						if (_data.getAll()[k][i+1][0].size() > (j+3)%4) {
							value += _data.getAll()[k][i+1][0].valueArray()[(j+3)%4];
							n += 1;
						}
					} else {
						if (_data.getAll()[k][i+1][0].size() > j) {
							value += _data.getAll()[k][i+1][0].valueArray()[j];
							n += 1;
						}
					}
				}
				value /= n;
				avgNum.put(key, value);
			}
			_average[i] = avgNum;
		}
	}
	
	/*
	 * The original data ends at 2010-11-26. 
	 * This method predicts values for 11-27 ~ 11-30 so that
	 * future predictions after 2010-12-1 can be done smoothly.
	 */
	public void fillMissingValues() {
		for (int i = 0; i < 5; i++) {
			// set initial input data
			double[] x = new double[_p];
			for (int j = _p; j > 0; j--) {
				x[_p-j] = _all[i][_all[i].length-j]; 
			}
			// resize _all array to accomodate new predictions
			int allPrev = _all[i].length;
			_all[i] = Arrays.copyOf(_all[i], allPrev+4);
			for (int k = allPrev; k < _all[i].length; k++) {
				double y =  this.predict(0, x);
				_all[i][k] = y;
				for (int l = 0; l < _p-1; l++) {
					x[l] = x[l+1];
				}
				x[_p-1] = y;
			}
		}
	}
	
	/*
	 * Generic predict methods that returns an output from an input
	 * using the optimal coefficients
	 */
	public double predict(int meter, double[] input) {
		double output = _coeff[meter][0];
		for (int i = 1; i < _coeff[meter].length; i++) {
			output += _coeff[meter][i]*input[i-1];
		}
		return output;
	}
	
	/*
	 * Below methods are updating methods for the prediction value on the graph
	 */
	
	/*
	 * Modification of Graph updateDate method to update corresponding index values
	 */
	public void updateDate() {
		_date = _datePicker.getValue();
		// Different action for 2010 Dec
		if (_date.getYear() == 2010) {
			_month = 0;
		    _week = _date.get(WeekFields.of(Locale.US).weekOfWeekBasedYear()) - 49;
		    if (_week < 0) {
		    	_week = 4;
		    }
		    _day = _date.getDayOfMonth()-1;
		    _season = 0;
		// Different action for 2011
		} else {
			_month = _date.getMonthValue()-1;
		    _week = _date.get(WeekFields.of(Locale.US).weekOfWeekBasedYear()) + 3;
		    _day = _date.getDayOfYear()+30;
	    	switch(_month) {
	    	case 0: case 1:
	    		_season = 0;
	    		break;
	    	case 2: case 3: case 4:
	    		_season = 1;
	    		break;
	    	case 5: case 6: case 7:
	    		_season = 2;
	    		break;
	    	case 8: case 9: case 10:
	    		_season = 3;
	    		break;
	    	default:
	    		break;
	    	}
		}
	}
	
	/*
	 * We generate predictions and store them into a separate data structure
	 * for all meters based on season change. 
	 * If a new input date has a different season value than before, then
	 * we predict all values for that season, and store these data.
	 * If a new input date has a same season, then no more predictions are made,
	 * and the value is simply called from the data structure.
	 */
	public void generatePredictions() {
		// Determine start date and end date for the season
		int start = 0;
		int end = 0;
		int startWeek = 0;
		int startMonth = 0;
		switch(_season) {
		case 0:
			end = 89;
			break;
		case 1:
			start = 90;
			end = 181;
			startWeek = 12;
			startMonth = 2;
			break;
		case 2:
			start = 182;
			end = 273;
			startWeek = 26;
			startMonth = 5;
			break;
		case 3:
			start = 274;
			end = 364;
			startWeek = 39;
			startMonth = 8;
			break;
		default:
			break;
		}
		// Perform predictions only when that season is not predicted yet.
		if (_predictions[0][0][end] == 0) {
			//make predictions for data
			for (int i = 0; i < 5; i++) {
				int j = start;
				// set initial input
				double[] x = new double[_p];
				switch(_season) {
				case 0:
					for (int k = 0; k < _p; k++) {
						x[k] = _all[i][_all[i].length-1-k]; 
					}
					break;
				case 1: case 2: case 3:
					for (int k = 0; k < _p; k++) {
						x[k] = _predictions[i][0][j-1+k]; 
					}
					break;
				default:
					break;
				}
				// variables
				ArrayList<Double> weeks = new ArrayList<Double>();
				ArrayList<Double> months = new ArrayList<Double>();
				double week = 0;
				double month = 0;
				double season = 0;
				int c = 0;
				// make predictions
				double y = 0;
				while (j <= end) {
					// predict a data
					y = this.predict(i,x);
					// add to the dataset
					_predictions[i][0][j] = y;
					// increment
					j += 1;
					// reset input data: pop the first, push new
					for (int k = 0; k < _p-1; k++) {
						x[k] = x[k+1];
					}
					x[_p-1] = y;
					// calculate week,month,season data
					week += y;
					month += y;
					season += y;
					c += 1;
					if (c%7 == 0) {
						weeks.add(week);
						week = 0;
					}
					int m = 0;
					int[] daysInMonth = {};
					switch(_season) {
					case 0:
						daysInMonth = Constants.WINTER;
						break;
					case 1:
						daysInMonth = Constants.SPRING;
						break;
					case 2:
						daysInMonth = Constants.SUMMER;
						break;
					case 3:
						daysInMonth = Constants.FALL;
						break;
					default:
						break;
					}
					if (c%daysInMonth[m] == 0) {
						months.add(month);
						month = 0;
						m += 1;
					}
				}
				// add week
				for (int k = 0; k < weeks.size(); k++) {
					_predictions[i][1][startWeek+k] = weeks.get(k);
				}
				// add month
				for (int k = 0; k < months.size(); k++) {
					_predictions[i][2][startMonth+k] = months.get(k);
				}
				// add season
				_predictions[i][3][_season] = season;
			}
		}
		//Set what to put on the average graph
		for (int i = 0; i < 4; i++) {
			_current[i] = _predictions[0][i][this.matchingIndex(i)];
		}
		//Set what to pass onto the House
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 4; j++) {
				_houseData[i][j] = _predictions[i][j][this.matchingIndex(j)];
			}
		}
	}
	
	/*
	 * Helper method that calls a corresponding index value for different time frame type
	 */
	public int matchingIndex(int a) {
		int index = -1;
		switch(a){
		case 0:
			index = _day;
			break;
		case 1:
			index = _week;
			break;
		case 2:
			index = _month;
			break;
		case 3:
			index = _season;
			break;
		default:
			break;
		}
		return index;
	}
	
	/*
	 * updates the average chart with a new prediction value
	 */
	public void updateChart(LocalDate date) {
		//input: prediction point matched with the current chart
		if (_date != date) {
			for (int i = 0; i < 4; i++) {
				_chartHolder[i].getData().remove(_series[i]);
				_series[i] = new XYChart.Series<String, Number>();
				
		        _series[i].setName("Predicted Value: " + _df.format(_current[i]) + "kW");
				String key = Constants.TYPES[i]+" "+String.valueOf(this.matchingIndex(i)+1);
				if (i == 3) {
					key = Constants.SEASONS[_season];
				}
		        _series[i].getData().add(new XYChart.Data<String, Number>(key,_current[i]));
				_chartHolder[i].getData().add(_series[i]);
			}
		}
	}
	
	/*
	 * JavaFX design setup of this pane
	 */
	public void setup(VBox contentPane) {
		Label content1 = new Label("What if we can predict future energy consumption "
				+ "with this data, using machine learning? \n"
				+ "Choose an option to predict (day, week, month, or season) and "
				+ "select a date of your choice.\n"
				+ "For example, if you choose 'Week,' choose any date of the week you want to predict.");
		content1.setStyle(Constants.FONT);
		//Buttons
		HBox optionBox = new HBox();
		optionBox.setSpacing(Constants.SPACING);
		optionBox.setAlignment(Pos.CENTER);
		Button start = new Button("Start");
		start.setOnAction(new ButtonHandler());
		_optionButton = new MenuButton("Week");
		_optionButton.setAlignment(Pos.CENTER);
		_optionButton.setStyle(Constants.FONT);
		_optionButton.setPrefWidth(Constants.WIDTH*Constants.BUTTON1_RATIO);
		MenuItem[] options = new MenuItem[4];
		MenuHandler menuHandler = new MenuHandler();
		for (int i = 0; i < 4; i++) {
			options[i] = new MenuItem(Constants.TYPES[i]);
			options[i].setOnAction(menuHandler);
			_optionButton.getItems().add(options[i]);
		}
		_datePicker = new DatePicker();
		_datePicker.setShowWeekNumbers(true);
		_datePicker.setValue(LocalDate.of(2010, 12, 1));
		_datePicker.setOnAction(menuHandler);
		_datePicker.setDayCellFactory(new CellFactory());
		optionBox.getChildren().addAll(start, _optionButton, _datePicker);
        //Result
		_result = new Pane();
		_result.setPrefSize(Constants.ML_SIZE[0],Constants.ML_SIZE[1]);
		_result.setMaxSize(Constants.ML_SIZE[0],Constants.ML_SIZE[1]);
		CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<String, Number>(xAxis, yAxis);
        _result.getChildren().add(chart);
		contentPane.getChildren().addAll(content1,optionBox,_result);
	}
	
	/*
	 * Getter methods used for House
	 */
	
	public String getDate() {
		return _date.toString();
	}
	
	public double[] getPredictions(int meter) {
		return _houseData[meter];
	}
	
	/*
	 * Start Button to generate initial prediction and average graph
	 */
	private class ButtonHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent e) {
			Prediction.this.updateDate();
			Prediction.this.generatePredictions();
			_result.getChildren().clear();
			_result.getChildren().add(_chartHolder[_chartNum]);
			Prediction.this.updateChart(null);
			((Button)e.getSource()).setDisable(true);
		}
	}
	
	/*
	 * MenuHandler similar to that of Graph, updating parameters for different menu items and datepicker,
	 * and updating new outputs with new inputs.
	 */
	private class MenuHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent e) {
			LocalDate prevDate = null;
			if (_date != null) {
				prevDate = _date;
			}
			Prediction.this.updateDate();
			//update parameters
			int chartNum = _chartNum;
			if (e.getSource() != _datePicker) {
				String clicked = ((MenuItem)e.getSource()).getText();
	        	_optionButton.setText(clicked);
	            _result.getChildren().clear();
	            switch(clicked) {
	            case "Day":
	            	chartNum = 0;
	            	break;
				case "Week":
	            	chartNum = 1;
					break;
				case "Month":
	            	chartNum = 2;
					break;
				case "Season":
	            	chartNum = 3;
					break;
	    		default:
	    			break;
	            }
			}
			//update chart if necessary
			if (_chartNum != chartNum) {
				_chartNum = chartNum;
				_result.getChildren().clear();
				_result.getChildren().add(_chartHolder[_chartNum]);
			}
			//make forecasts upto the selected date. If date is already predicted, merely return the 
			//already made predictions
			//you make prediction for all 5 types: total to all meters
			Prediction.this.generatePredictions();
			//add the updated point of the selected date onto the graph. 
			// If date is chagned, erase and update new one
			Prediction.this.updateChart(prevDate);
            e.consume();
		}
	}
	
	/*
	 * Modifies the Calendar only to the year that we will predict.
	 */
	private class CellFactory implements Callback<DatePicker, DateCell> {

		@Override
		public Cell call(DatePicker datePicker) {
			return new Cell();
		}
		
		private class Cell extends DateCell {
			
			@Override
			public void updateItem(LocalDate item, boolean empty) {
				super.updateItem(item, empty);
				int year = item.getYear();
				int month = item.getMonthValue();
				if (year!=2011) {
					this.setDisable(true);
				} else {
					if (month == 12) {
						this.setDisable(true);
					}
					if (month == 11 && item.getDayOfMonth() > 26) {
						this.setDisable(true);
					}
				}
				if (year == 2010 && month == 12) {
					this.setDisable(false);
				}
				
			}
		}
	}
}
