package Indy;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.Calendar;
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
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * This is Graph class. Graph class receives data from Data class
 * and visualizes them into various graphs.
 * The methods include a generic graphing method, selection methods
 * that updates the graph for different inputs of the menu,
 * and JavaFX design setup methods for various buttons.
 */
public class Graph {

	private XYChart<String,Number>[][][][] _chartHolder;
	private VBox _graph;
	private MenuButton _typeButton, _dataButton, _graphButton;
	private DatePicker _datePicker;
	private Data _data;
	private Numbers _particular;
	private MenuHandler _handler;
	private Label _dummy;
	private String _season;
	private LocalDate _date, _checker;
	private int _year, _month, _week, _day, _type, _dataType, _graphType;
	
	/*
	 * Constructor: warning for instantiating a generic multi-dimenstional array of Charts was suppressed
	 * This was necessary to collect all datasets into one data structure; 
	 * handling them was more efficient this way.
	 */
	@SuppressWarnings("unchecked")
	public Graph(VBox contentPane, Data data) {
		_data = data;
		_handler = new MenuHandler();
		//[year][timeFrame][meter][graphType]
		_chartHolder = new XYChart[4][5][5][2]; 
		// Dummy Label to show when no data exists for picked inputs. Usually not called.
		_dummy = new Label("No data exists for this category. Choose a different one.");
		_dummy.setStyle("-fx-font-size:30");
		this.setup(contentPane);
	}
	
	/*
	 * Graphing method
	 */
	public void generateGraph(Numbers data, int graphType) {
		// graph components
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<String, Number>(xAxis, yAxis);
        XYChart.Series<String,Number> series = new XYChart.Series<String, Number>();
        series.setName(String.join(" ", "Data of",String.valueOf(_year+2007),"by",_typeButton.getText(),
        		"-",_dataButton.getText(),"("+_graphButton.getText()+")"));
        // determine x-axis inputs
        for (int i = 0; i < data.size(); i++) {
        	String key = data.keyArray()[i];
        	if (graphType == 0) {
        		switch(_type) {
            	case 1: case 3: case 4:// "yyyy-mm-dd", "yyyy-mm","yyyy-season"
            		String[] parsed = key.split("-");
            		key = String.join("-", Arrays.copyOfRange(parsed, 1, parsed.length));
            		break;
            	case 2: // "yyyy-mm-dd-yyyy-mm-dd Week w"
            		key = key.split(" ")[2];
            		break;
        		default:
        			break;
            	}
        	} else {
        		switch(_type) {
            	case 1: // "yyyy--mm-dd hh"
            		key = key.split(" ")[1];
            		break;
            	case 2: // "yyyy-mm-dd"
            		key = Constants.WEEKDAYS[i];
            		break;
            	case 3: case 4:// "yyyy-mm-dd"
            		String[] parsed = key.split("-");
            		key = parsed[parsed.length-1];
            		break;
        		default:
        			break;
            	}
        	}
        	series.getData().add(new XYChart.Data<String, Number>(key, (Number)data.valueArray()[i]));
        }
        chart.getData().add(series);
        chart.setCreateSymbols(false);
        _chartHolder[_year][_type][_dataType][graphType] = chart;
	}
	
	/*
	 * Selection methods
	 */
	
	/*
	 * updates instance variables of date data for a new input from the datePicker
	 */
	public void updateDate() {
		_checker = _date;
		_date = _datePicker.getValue();
	    _year = _date.getYear()-2007;
	    _month = _date.getMonthValue();
	    _week = _date.get(WeekFields.of(Locale.US).weekOfWeekBasedYear());
	    _day = _date.getDayOfMonth();
		_season = "";
    	switch(_month) {
    	case 12: 
    		_season = String.valueOf(_year+2007) + "-12";
    		break;
    	case 1: case 2:
    		/* if a date between 2007-01-01 and 2007-02-28 is chosen,
    		 * 2006-12 is returned and this is not a part of data.
    		 * So generateGraph method above takes care of this edge case.
    		 */
    		_season = String.valueOf(_year+2006) + "-12";
    		break;
    	case 3: case 4: case 5:
    		_season = String.valueOf(_year+2007) + "-3";
    		break;
    	case 6: case 7: case 8:
    		_season = String.valueOf(_year+2007) + "-6";
    		break;
    	case 9: case 10: case 11:
    		_season = String.valueOf(_year+2007) + "-9";
    		break;
    	default:
    		break;
    	}
	}
	
	/*
	 * update the graph data based on new inputs of menu and date
	 */
	public void update() {
		if (_date != _checker) {
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 5; j++) {
					for (int k = 0; k < 5; k++) {
						_chartHolder[i][j][k][1] = null;
					}
				}
			}
		}
		//Data Type
		switch(_dataButton.getText()) {
    	case "Total": 
        	_dataType = 0; 
        	break;
        case "Sub_meter_1": 
        	_dataType = 1;  
        	break;
        case "Sub_meter_2": 
        	_dataType = 2; 
        	break;
        case "Sub_meter_3": 
        	_dataType = 3;  
        	break;
        case "Sub_meter_4":
        	_dataType = 4; 
        	break;
        default:
        	break;
    	}
		//Type
    	switch(_typeButton.getText()) {
		case "Day":
	        _type = 1;
	        String day = String.join("-", String.valueOf(_year+2007),String.valueOf(_month),String.valueOf(_day));
	        _particular = _data.search(_data.getAll()[_year][0][_dataType], day, 0, _dataType);
			break;
		case "Week":
        	_type = 2;
			SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, _year+2007);
			cal.set(Calendar.MONTH, _month);
			cal.set(Calendar.WEEK_OF_YEAR, _week);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			int[] dates = Arrays.stream(dayFormat.format(cal.getTime()).split("-")).
					mapToInt(Integer::parseInt).toArray(); 
			String startDay = String.join("-", String.valueOf(dates[0]),
					String.valueOf(dates[1]),String.valueOf(dates[2]));
			_particular = _data.search(_data.getAll()[_year][1][_dataType], startDay, 1, _dataType);
        	break;
        case "Month": 
        	_type = 3;
        	String yearMonth = String.valueOf(_year+2007) + "-" + String.valueOf(_month);
        	 _particular = _data.search(_data.getAll()[_year][1][_dataType], yearMonth, 2, _dataType);
        	break;
        case "Season":
        	_type = 4;
        	_particular = _data.search(_data.getAll()[_year][3][_dataType], _season, 3,_dataType);
        	break;
        default:
        	break;
    	}
    	//GraphType
    	switch(_graphButton.getText()) {
		case "Year-long": 
        	_graphType = 0;  
        	break;
        case "Selected Time-frame":
        	_graphType = 1; 
        	break;
        default:
        	break;
		}
		// If graph is not yet created, create the graph. 
        // Saves runtime when we want to see the same graph again.
		if (_chartHolder[_year][_type][_dataType][0] == null) {
			this.generateGraph(_data.getAll()[_year][_type][_dataType],0);
        }
        if (_chartHolder[_year][_type][_dataType][1] == null) {
        	this.generateGraph(_particular,1);
        }
        //Based on the graphType, show the corresponding graph.
        _graph.getChildren().clear();
        if (_year == -1 && _month == 12) {
        	_graph.getChildren().add(_dummy);
		} else {
			_graph.getChildren().add(_chartHolder[_year][_type][_dataType][_graphType]);
        }
	}
	
	/*
	 * JavaFX Deisng setup
	 */
	public void setup(VBox contentPane) {
		//Menu options
		HBox optionBox = new HBox();
		optionBox.setSpacing(Constants.SPACING);
		optionBox.setAlignment(Pos.CENTER);
		//Buttons
		Button start = new Button("Start");
		start.setOnAction(new ButtonHandler());
		_datePicker = new DatePicker();
		_datePicker.setShowWeekNumbers(true);
		_datePicker.setValue(LocalDate.of(2007, 1, 1));
		_datePicker.setOnAction(_handler);
		_datePicker.setDayCellFactory(new CellFactory());
		MenuItem[] options = new MenuItem[4];
		for (int i = 0; i < 4; i++) {
			options[i] = new MenuItem(Constants.TYPES[i]);
		}
		_typeButton = this.setMenu(_typeButton, "Week", options, 4, Constants.BUTTON1_RATIO);
		MenuItem[] dataTypes = new MenuItem[5];
		for (int i = 0; i < 5; i++) {
			dataTypes[i] = new MenuItem(Constants.DATATYPES[i]);
		}
		_dataButton = this.setMenu(_dataButton, "Total", dataTypes, 5, Constants.BUTTON2_RATIO);		
		MenuItem[] graphTypes = new MenuItem[2];
		graphTypes[0] = new MenuItem("Year-long");
		graphTypes[1] = new MenuItem("Selected Time-frame");
		_graphButton = this.setMenu(_graphButton, "Selected Time-frame", graphTypes, 2, Constants.BUTTON3_RATIO);				
        optionBox.getChildren().addAll(start, _datePicker, _typeButton, _dataButton, _graphButton);
		//actual graph
		_graph = new VBox();
		_graph.setPrefSize(Constants.GRAPH_SIZE[0],Constants.GRAPH_SIZE[1]);
		_graph.setMaxSize(Constants.GRAPH_SIZE[0],Constants.GRAPH_SIZE[1]);
		_graph.setAlignment(Pos.CENTER);
		CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<String, Number>(xAxis, yAxis);
        _graph.getChildren().add(chart);
		contentPane.getChildren().addAll(optionBox,_graph);
	}
	
	/*
	 * Helper method to design menubuttons in this pane
	 */
	public MenuButton setMenu(MenuButton button, String name, 
			MenuItem[] items, int numOfItems, double ratio) {
		button = new MenuButton(name);
		button.setAlignment(Pos.CENTER);
		button.setStyle(Constants.FONT);
		button.setPrefWidth(Constants.WIDTH*ratio);
		for (int i = 0; i < numOfItems; i++) {
			items[i].setOnAction(_handler);
			button.getItems().add(items[i]);
		}
		return button;
	}
	
	/*
	 * ButtonHandler for the start
	 */
	private class ButtonHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent e) {
			((Button)e.getSource()).setDisable(true);
			Graph.this.updateDate();
			Graph.this.update();
			e.consume();
		}
		
	}
	
	/*
	 * MenuHandler for different parametes and datePicker
	 */
	private class MenuHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent e) {
			Graph.this.updateDate();
			if (e.getSource() != _datePicker) {
				String clicked = ((MenuItem)e.getSource()).getText();
	            switch(clicked) {
	            case "Day": case "Week": case "Month": case "Season":
	            	_typeButton.setText(clicked);
	            	break;
	            case "Total": case "Sub_meter_1": case "Sub_meter_2": 
	            	case "Sub_meter_3":  case "Sub_meter_4":
	            	_dataButton.setText(clicked);
					break;
	        	case "Year-long": case "Selected Time-frame":
	        		_graphButton.setText(clicked);
	        		break;
	    		default:
	    			break;
	            }
			}
			Graph.this.update();
			e.consume();
		}
	}
	
	/*
	 * Modified the Calendar so that only available dates for the existing data can be picked.
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
				if (year < 2007) {
					this.setDisable(true);
				} else if (year >= 2010) {
					if (year == 2010) {
						if (item.getDayOfYear() > 330) {
							this.setDisable(true);
						}
					} else {
						this.setDisable(true);
					}
				}
			}
		}
	}
}
