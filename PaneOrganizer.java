package Indy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import Indy.Data.Datum;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *  This is PaneOrganizer, the top-level class of this package.
 *  This deals with the general JavaFX design of this UI,
 *  concerning various types of panes, their locations and sizes,
 *  and corresponding lower classes that deals with the inner components
 *  of the sections. 
 *  The sectiosn are divdied into: Title, Data, Graph, Prediction, and House.
 *  Due to the issue of concurrency for updating the console Label,
 *  the buttonhandler that deals with the components in the data section
 *  was pulled up to this higher level object. 
 *  It was more appropriate to design Thread-related codes on the top-level object.
 *  
 */
public class PaneOrganizer {

	/*
	 * Window class used for House, console for updating the process of data upload.
	 * Data and Prediction methods are instance variables as they have methods
	 * called in this class.
	 */
	private GridPane _root;
	private Label _console;
	private Window _window;
	private Data _data;
	private Prediction _prediction;
	
	/*
	 * Constructor: associates the stage and calls all design methods.
	 */
	public PaneOrganizer(Stage stage) {
		_window = stage;
		_root = new GridPane();
		_root.setStyle("-fx-background-color: white");
		this.setRoot();
		this.setTitleSection();
		this.setDataSection();
		this.setGraphSection();
		this.setPredictionSection();
		this.setHouseSection();
	}
	
	/*
	 * Sets the title and the console on the left corner. 
	 */
	public void setTitleSection() {
		StackPane titlePane = new StackPane();
		_root.add(titlePane, 0, 0, 3, 1);
		titlePane.setAlignment(Pos.TOP_CENTER);
		titlePane.setPadding(Constants.PADDING3);
		Label title = new Label("Energy Data Management");
		String style = "-fx-font-size:" + Constants.HEIGHT*Constants.TITLE_RATIO;
		VBox consoleBox = new VBox();
		consoleBox.setAlignment(Pos.CENTER_LEFT);
		consoleBox.setPadding(Constants.PADDING);
		_console = new Label();
		_console.setStyle(Constants.FONT);
		consoleBox.getChildren().add(_console);
		title.setStyle(style);
		titlePane.getChildren().addAll(title,consoleBox);
	}
	
	/*
	 * From now on, each section has its method for its pane organiztion.
	 */
	
	/*
	 * Explanation of the data and the project
	 */
	public void setDataSection() {
		_data = new Data();
		VBox dataPane = this.makePane(0, 1, 2, 1);
		dataPane.setSpacing(Constants.SPACING);
		//Title
		Label title = this.setSubTitle("What is this?");
		//Content
		VBox contentPane = new VBox();
		contentPane.setSpacing(Constants.SPACING);
		Label content1 = new Label("I acquired a real household's electric power consumption data from 2007 to 2010."
				+ " Each datum represents a minute of usage. \nA sample from the data looks like this:");
		GridPane table1 = this.makeTable(Constants.DATA1, "");
		table1.setAlignment(Pos.CENTER);
		Label content2 = new Label("Below are explanations of the parameters:\n"
				+ "- Active_power vs Reactive_power: The real power consumed "
				+ "vs the unused power in the lines (unit: kW)\n"
				+ "- Voltage, Intensity: Average voltage and current (unit: V,A)\n"
				+ "- Sub_meter_X: Energy used in a specific area of the house  "
				+ "(1: Kitchen),(2:Laundry),(3:Climate-Control)  (unit: WH)\n\n"
				+ "To help visualize the data, I summed up each data value into hour, day, week, month, season. "
				+ "An example may look like this:");
		GridPane table2 = this.makeTable(Constants.DATA2, "Time");
		table2.setAlignment(Pos.CENTER);
		Label content3 = new Label("Below are buttons to load the data to the program and to "
				+ "save each divided dataset. Press the save buttons to save data tables "
				+ "\ndivided by each category into separate files. Press Load Data before saving any data.");
		// Buttons
		HBox buttonBox = new HBox();
		buttonBox.setSpacing(4*Constants.SPACING);
		buttonBox.setAlignment(Pos.CENTER);
		ButtonHandler handler = new ButtonHandler();
		for (int i = 0; i < Constants.BUTTONS.length; i++) {
			ToggleButton button = new ToggleButton(Constants.BUTTONS[i]);
			button.setOnAction(handler);
			buttonBox.getChildren().add(button);
		}
		Label content4 = new Label(
				"\nOn the section below, you can choose some parameters to see some specific data as a graph. "
				+ "Choose a date, the range of \ndata (day, week, month, or season "
				+ "of the date), and data type (for each metered section of the house). "
				+ "Press Load Data, and then Start.");
		Label[] contents = {content1, content2, content3, content4};
		for (int i = 0; i < 4; i++) {
			contents[i].setStyle(Constants.FONT);
		}
		contentPane.getChildren().addAll(content1,table1,content2,table2,content3,buttonBox,content4);
		dataPane.getChildren().addAll(title,contentPane);
	}
	
	/*
	 * visualization of the data
	 */
	public void setGraphSection() {
		VBox graphPane = this.makePane(0, 2, 1, 2);
		graphPane.setAlignment(Pos.TOP_CENTER);
		graphPane.setSpacing(Constants.SPACING);
		//Content
		VBox contentPane = new VBox();
		contentPane.setAlignment(Pos.CENTER);
		contentPane.setSpacing(Constants.SPACING);
		//Call Graph class
		new Graph(contentPane, _data);
		graphPane.getChildren().addAll(contentPane);
	}
	
	/*
	 *  machine learning input section
	 */
	public void setPredictionSection() {
		VBox predictionPane = this.makePane(2, 1, 1, 2);
		predictionPane.setSpacing(Constants.SPACING);
		//Title
		Label title = this.setSubTitle("Machine Learning");
		//Content
		VBox contentPane = new VBox();
		contentPane.setAlignment(Pos.CENTER);
		contentPane.setSpacing(Constants.SPACING);
		_prediction = new Prediction(contentPane, _data);
		predictionPane.getChildren().addAll(title,contentPane);
	}
	
	/*
	 * house gui
	 */
	public void setHouseSection() {
		VBox housePane = this.makePane(1, 2, 2, 2);
		StackPane stack = new StackPane();
		Label content = new Label("Click a room in the house above to see its future energy usage on the picked date.\n"
				+ "Options include: Kitchen, Laundry Room, Bathrooms, and Living Room/Bedrooms\n ");
		content.setTextAlignment(TextAlignment.CENTER);
		content.setStyle(Constants.FONT);
		housePane.getChildren().addAll(stack,content);
		new House(_window, stack, _prediction);
	}
	
	/*
	 * Helper Methods
	 */
	
	/*
	 * Grid's cell styling
	 */
	public VBox makePane(int col, int row, int colNum, int rowNum) {
		VBox pane = new VBox();
		pane.setPadding(Constants.PADDING);
		_root.add(pane, col, row, colNum, rowNum);
		pane.setAlignment(Pos.TOP_CENTER);
		return pane;
	}
	
	/*
	 * table creation method
	 */
	public GridPane makeTable(String[] data, String skip) {
		GridPane table = new GridPane();
		table.setGridLinesVisible(true);
		int i = 0;
		int j = 0;
		while (i < Constants.CATEGORIES.length & j < data.length) {
			if (Constants.CATEGORIES[i] == skip) {
				i += 1;
			}
			table.add(new Label(Constants.CATEGORIES[i]), j, 0);
			table.add(new Label(data[j]), j, 1);
			i += 1;
			j += 1;
		}
		for (int k = 0; k < table.getChildren().size(); k++) {
			table.getChildren().get(k).setStyle(Constants.FONT);
			GridPane.setMargin(table.getChildren().get(k), Constants.TABLE_MARGIN);
			GridPane.setHalignment(table.getChildren().get(k), HPos.CENTER);
		}
		return table;
	}

	/*
	 * subtitle styling method
	 */
	public Label setSubTitle(String content) {
		Label subtitle = new Label(content);
		subtitle.setStyle(Constants.SUBTITLE);
		return subtitle;
	}
	
	/* 
	 * Accessor and Mutator Methods
	 */
	
	public void setRoot() {
	    _root.setPadding(Constants.PADDING);
	    for (int i = 0; i < Constants.ROOT_COLS.length; i++) {
		    ColumnConstraints col = new ColumnConstraints();
		    col.setPercentWidth(Constants.ROOT_COLS[i]);
		    _root.getColumnConstraints().add(col);
	    }
	    for (int j = 0; j < Constants.ROOT_ROWS.length; j++) {
	    	RowConstraints row = new RowConstraints();
		    row.setPercentHeight(Constants.ROOT_ROWS[j]);
		    _root.getRowConstraints().add(row);
	    }
	}
	
	public GridPane getRoot() {
		return _root;
	}
		
	private class ButtonHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent e) {
			switch(((ToggleButton)e.getSource()).getText()) {
			case "Load Data":
				// Concurrency is used to update label during a time-consuming process
				// to update the user on where the process currently is.
				LoadRunnable load = new LoadRunnable(e);
				Thread thread = new Thread(load);
				thread.setDaemon(true);
				thread.start();
				break;
			// writing methods
			case "Save Minute":
				this.write(_data.getData(), "power_consumption_minute.csv");
				((ToggleButton)e.getSource()).setDisable(true);
				break;
			case "Save Hour":
				this.write(_data.getDatasets()[0], "power_consumption_hour.csv");
				((ToggleButton)e.getSource()).setDisable(true);
				break;
			case "Save Day":
				this.write(_data.getDatasets()[1], "power_consumption_day.csv");
				((ToggleButton)e.getSource()).setDisable(true);
				break;
			case "Save Week":
				this.write(_data.getDatasets()[2], "power_consumption_week.csv");
				((ToggleButton)e.getSource()).setDisable(true);
				break;
			case "Save Month":
				this.write(_data.getDatasets()[3], "power_consumption_month.csv");
				((ToggleButton)e.getSource()).setDisable(true);
				break;
			case "Save Season":
				this.write(_data.getDatasets()[4], "power_consumption_season.csv");
				((ToggleButton)e.getSource()).setDisable(true);
				break;
			default:
				break;
			}
			e.consume();
		}
		
		// write algorithm
		public void write(ArrayList<Datum> data, String name){
			try {
				BufferedWriter writer = new BufferedWriter(
						new FileWriter(new File(Constants.PATH+name)));
				if (writer != null) {
					
				}
				String delimeter = ",";
				writer.write("DateTime,");
				writer.write(String.join(delimeter, Constants.KEYS));
				writer.newLine();
				for (int i = 0; i < data.size(); i++) {
					writer.write(data.get(i).getKey()+delimeter);
					String[] values = new String[8];
					for (int j = 0; j < 8; j++) {
						values[j] = String.valueOf(data.get(i).getValue().get(Constants.KEYS[j]));
					}
					writer.write(String.join(delimeter, values));
					writer.newLine();
				}
				writer.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Below methods are subclasses of Runnable that manages concurrency
	 * We can see that in LoadRunnable, new TextRunnables are called
	 * to update the Label everytime a new process is run.
	 */
	private class LoadRunnable implements Runnable {
		
		private ActionEvent _e;
		
		public LoadRunnable(ActionEvent e) {
			_e = e;
		}
		
		@Override
		public void run() {
			((ToggleButton)_e.getSource()).setSelected(true);;
			String loading = "Please wait while loading. This might take awhile.\n";
			// initial load
			Platform.runLater(new TextRunnable(loading+"Uploading text file..."));
			_data.loadData(Constants.PATH+ "household_power_consumption.txt");
			// data parsing into different time frame
			Platform.runLater(new TextRunnable(loading+"Parsing hourly data..."));
			_data.hourly();
			Platform.runLater(new TextRunnable(loading+"Parsing daily data..."));
			_data.daily();
			Platform.runLater(new TextRunnable(loading+"Parsing weekly data..."));
			_data.weekly();
			Platform.runLater(new TextRunnable(loading+"Parsing monthly data..."));
			_data.monthly();
			Platform.runLater(new TextRunnable(loading+"Parsing seasonal data..."));
			_data.seasonal();
			//Store individual data into a structure
			for (int i = 0; i < 4; i++) {
				Platform.runLater(new TextRunnable(loading+"Categorizing "
						+ "data for year "+String.valueOf(2007+i)+"..."));
				for (int j = 0; j < 5; j++) {
					for (int k = 0; k < 5; k++) {
						_data.getAll()[i][j][k] = _data.cutByCategory(
								_data.cutByYear(_data.getDatasets()[j],2007+i),Constants.KEYS2[k]);
					}
				}
			}
			// ML preliminary actions
			Platform.runLater(new TextRunnable(loading+"Splitting train and test data..."));
            _prediction.splitData();
			Platform.runLater(new TextRunnable(loading+"Training machine learning model..."));
            _prediction.setOptimalParam();
			Platform.runLater(new TextRunnable(loading+"Calculating mean values..."));
            _prediction.generateAverageGraphs();
            _prediction.fillMissingValues();
            // Final
			Platform.runLater(new TextRunnable("Data upload complete!"));
			((ToggleButton)_e.getSource()).setDisable(true);
		}
	}

	private class TextRunnable implements Runnable {

		private String _text;
		
		public TextRunnable(String text) {
			_text = text;
		}
		
		@Override
		public void run() {
			_console.setText(_text);
		}
		
	}
}
