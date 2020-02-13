package Indy;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.text.DecimalFormat;

import javafx.geometry.Insets;

/*
 * This is Constants class. Different data for differet categories are organized.
 */
public class Constants {
	
	//File Path
	public static final String PATH = System.getProperty("user.dir") + "/Indy/";
	
	//Display
	public static final Dimension SCREENSIZE = Toolkit.getDefaultToolkit().getScreenSize();
	public static final double WIDTH = SCREENSIZE.getWidth();
	public static final double HEIGHT = SCREENSIZE.getHeight();
	public static final double CONSOLE_WIDTH = WIDTH/8;
	
	//Font Styles
	public static final double TITLE_RATIO = 0.05*0.7;
	public static final double SUBTITLE_RATIO = 0.05*0.5;
	public static final String SUBTITLE = "-fx-font-size:" + HEIGHT*SUBTITLE_RATIO;
	public static final String FONT = "-fx-font-size:" + HEIGHT*0.014;
	
	//Paddings and Spacings
    public static final int[] ROOT_COLS = {50,8,42};
    public static final int[] ROOT_ROWS = {7,43,5,45};
	public static final double WINDOW_PADDING = 0.005;
	public static final double TABLE_RATIO = 0.002;
	public static final double SPACING =HEIGHT*WINDOW_PADDING;
	public static final double H_PADDING = SPACING;
	public static final double W_PADDING = WIDTH*WINDOW_PADDING;
	public static final Insets PADDING = new Insets(H_PADDING, W_PADDING, 0, W_PADDING);
	public static final Insets PADDING2 = new Insets(H_PADDING, W_PADDING, H_PADDING, W_PADDING);
	public static final Insets PADDING3 = new Insets(3*Constants.H_PADDING,0,3*Constants.H_PADDING,0);
	public static final double TABLE_PADDING = HEIGHT*TABLE_RATIO;
	public static final Insets TABLE_MARGIN = new Insets(TABLE_PADDING,2*TABLE_PADDING,TABLE_PADDING,2*TABLE_PADDING);
	public static final Insets GRAPH_MARGIN = new Insets(10*SPACING,10*SPACING,10*SPACING,10*SPACING);
	
	// Component Display settings
	public static final double[] GRAPH_SIZE = {WIDTH*0.5*0.95,HEIGHT*0.42};
	public static final double[] HOUSE_SIZE = {WIDTH*0.5*0.95,HEIGHT*0.37*1.12};
	public static final double[] IMAGE_SIZE = {HOUSE_SIZE[0]*0.8,HOUSE_SIZE[1]};
	public static final double[] ML_SIZE = {WIDTH*0.4*0.95,HEIGHT*0.26};
	public static final double BUTTON1_RATIO = 0.06;
	public static final double BUTTON2_RATIO = 0.08;
	public static final double BUTTON3_RATIO = 0.12;
	
	//House Sizes
	public static final int[] COLS = {28,12,19,20,19};
	public static final int[] ROWS = {11,32,9,4,24,31};
	public static final String[] AREAS = {"Bathroom 1","Bathroom 2","Kitchen","Laundry"};
	public static final int[][] LOC = {{1,1,1,2},{4,4,1,1},{3,1,1,1},{4,1,1,3}};
	
	//Data Keys and Values
	public static final String[] CATEGORIES = {"Date","Time","Active_power","Reactive_power","Voltage",
			"Intensity","Sub_meter_1","Sub_meter_2","Sub_meter_3","Sub_meter_4"};
	public static final String[] KEYS = {"Active_power","Reactive_power","Voltage",
			"Intensity","Sub_meter_1","Sub_meter_2","Sub_meter_3","Sub_meter_4"};
	public static final String[] TYPES = {"Day","Week","Month","Season"};
	public static final int[] TYPE_NUM = {365,52,12,4};
	public static final String[] DATATYPES = {"Total","Sub_meter_1","Sub_meter_2","Sub_meter_3","Sub_meter_4"};
	public static final String[] KEYS2 = {"Active_power","Sub_meter_1","Sub_meter_2","Sub_meter_3","Sub_meter_4"};
	public static final int DAY = 60*24;
	public static final DecimalFormat DF = new DecimalFormat("#.##");
	public static final String[] SEASONS = {"Winter", "Spring", "Summer", "Fall"};
	public static final int MAX_PARAM = 100;
	public static final int[] WINTER = {31,31,28};
	public static final int[] SPRING = {31,30,31};
	public static final int[] SUMMER = {30,31,31};
	public static final int[] FALL = {30,31,30};
	public static final String[] WEEKDAYS = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
	
	//Data Samples
	public static final String[] BUTTONS = {"Load Data","Save Minute","Save Hour","Save Day","Save Week","Save Month","Save Season"};
	public static final String[] DATA1 = {"16/12/2006","17:24:00","4.216","0.418","234.840","18.400","0.000","1.000","17.000"};
	public static final String[] DATA2 = {"2006-12-16","1209.18","34.92","236.24","13.08","0.0","546.0","4926.0","14684.0"};
	
}
