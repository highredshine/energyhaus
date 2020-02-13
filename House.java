package Indy;

import java.text.DecimalFormat;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;

/*
 * This is House class. It represents the GUI where clicking
 * certain area of the house shows data of power consumption
 * of this area of the predicted date. 
 * The class contains the graphical setup of this house
 * and the eventhandlers to manage the popups
 */
public class House {
	
	private Window _window;
	private Popup _popup;
	private int _meter;
	private Prediction _prediction;
	private DecimalFormat _df;

	/*
	 * Constructor: House class associates a Window class to be matched 
	 * with the stage object of the PaneOrganizer
	 */
	public House(Window window, StackPane housePane, Prediction prediction) {
		_window = window;
		_prediction = prediction;
		_popup = new Popup();
		_df = new DecimalFormat(".##");
		this.setup(housePane);
	}
	
	/*
	 * House setup
	 */
	public void setup(StackPane housePane) {
		// 3D Image of the House
		ImageView image = new ImageView(new Image(this.getClass().getResourceAsStream("img.jpg")));
		image.setFitWidth(Constants.HOUSE_SIZE[0]);
		image.setFitHeight(Constants.HOUSE_SIZE[1]);
		// Create Grid Pane for different area of the house
		GridPane grid = new GridPane();
	    for (int i = 0; i < 5; i++) {
			ColumnConstraints col = new ColumnConstraints();
		    col.setPercentWidth(Constants.COLS[i]);
		    grid.getColumnConstraints().add(col);
	    }
	    for (int i = 0; i < 6; i++) {
			RowConstraints row = new RowConstraints();
		    row.setPercentHeight(Constants.ROWS[i]);
		    grid.getRowConstraints().add(row);
	    }
	    grid.setId("");
	    MouseHandler handler = new MouseHandler();
	    for (int i = 0; i < 4; i++) {
	    	Pane room = new Pane();
			room.setId(Constants.AREAS[i]);
			room.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);
			grid.add(room, Constants.LOC[i][0], Constants.LOC[i][1], Constants.LOC[i][2], Constants.LOC[i][3]);
	    }
	    grid.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);
		housePane.getChildren().addAll(image,grid);
	}
	
	/*
	 * MouseHandler creates different actions for different area of the house
	 */
	private class MouseHandler implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent e) {
			// update input
			_popup.getContent().clear();
			String area = ((Pane)e.getSource()).getId();
			switch(area) {
			case "Kitchen":
				_meter = 1;
				break;
			case "Laundry":
				_meter = 2;
				break;
			case "Bathroom 1": case "Bathroom 2":
				_meter = 3;
				break;
			default:
				area = "Living Room / Bedrooms";
				_meter = 4;
				break;
			}
			// Create Pop up window
			StackPane root = new StackPane();
			Pane blurEffect = new Pane();
			blurEffect.setStyle("-fx-background-color:lightgrey;-fx-opacity:0.6");
			VBox content = new VBox();
			content.setSpacing(Constants.SPACING);
			content.setPadding(Constants.PADDING2);
			Label title = new Label(area);
			title.setStyle(Constants.SUBTITLE);
			Label date = new Label("Date: " + _prediction.getDate());
			Label subtitle = new Label("Predictions");
			date.setStyle(Constants.FONT);
			subtitle.setStyle(Constants.FONT);
			GridPane table = new GridPane();
			for (int i = 0; i < 4; i++) {
				table.add(new Label(Constants.TYPES[i]), 0, i);
				table.add(new Label(_df.format(_prediction.getPredictions(_meter)[i])+"kWH"), 1, i);
			}
			for (int k = 0; k < table.getChildren().size(); k++) {
				table.getChildren().get(k).setStyle(Constants.FONT);
				GridPane.setMargin(table.getChildren().get(k), Constants.TABLE_MARGIN);
			}
			Button close = new Button("close");
			close.setOnAction(new ButtonHandler());
			content.getChildren().addAll(title,date,subtitle,table,close);
			root.getChildren().addAll(blurEffect,content);
			_popup.getContent().add(root);
			_popup.show(_window, e.getSceneX(), e.getSceneY());
			e.consume();
		}
	}
	
	/*
	 * Implements a clsose button for each popup window
	 */
	private class ButtonHandler implements EventHandler<ActionEvent>{

		@Override
		public void handle(ActionEvent e) {
			_popup.hide();
			e.consume();
		}
		
	}
}
