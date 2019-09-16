package it.polito.s234844.thesis;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;

import it.polito.s234844.thesis.model.Order;
import it.polito.s234844.thesis.model.ThesisModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class SimulationController {
	
	private ThesisController home;
	private ThesisModel model;
	private HashMap<String, Integer> orderMap;
	private LocalDate orderDate;
	
	private final String error = "-fx-border-color: red; -fx-border-width: 2px;";
	private final String noError ="-fx-border-color: none;";
	
	@FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Pane simulationTop;

    @FXML
    private VBox vboxMain;

    @FXML
    private DatePicker simulationStartDate;

    @FXML
    private DatePicker simulationEndDate;

    @FXML
    private ComboBox<Integer> cbYear;

    @FXML
    private TextField txtLines;

    @FXML
    private TextField txtWaitingDays;

    @FXML
    private Button btnSimulate;
    
    @FXML
    private GridPane gridOrderDates;

    @FXML
    private Label txtStartDate;

    @FXML
    private Label txtEndDate;

    @FXML
    private Label txtNonProducedParts;

    @FXML
    private ListView<Order> listView;

    @FXML
    private GridPane ordersQuantityGrid;

    @FXML
    private Label txtOrdersPercentage;

    @FXML
    private Label txtQuantityPercentage;

    @FXML
    private Label txtOrders;

    @FXML
    private Label txtQuantity;

    @FXML
    private ProgressBar barOrders;

    @FXML
    private ProgressBar barQuantity;

    @FXML
    private GridPane statistics2Grid;

    @FXML
    private Label txtTotalIdleness;

    @FXML
    private Label txtLostOrders;

    @FXML
    private Label txtNoDelayOrders;

    @FXML
    private Label txtInStockOrders;

    @FXML
    private VBox vboxMain1;

    @FXML
    private DatePicker simulationStartDate1;

    @FXML
    private DatePicker simulationEndDate1;

    @FXML
    private ComboBox<Integer> cbYear1;

    @FXML
    private TextField txtLines1;

    @FXML
    private TextField txtWaitingDays1;

    @FXML
    private TextField txtSimulations;

    @FXML
    private Button btnSimulate1;

    @FXML
    private GridPane gridSuccessRate;

    @FXML
    private Label txtSuccessRate;

    @FXML
    private ProgressBar barSuccessRate;

    @FXML
    private GridPane gridIncompleteRate;

    @FXML
    private Label txtIncompleteRate;

    @FXML
    private ProgressBar barIncompleteRate;

    @FXML
    private GridPane gridFailuresRate;

    @FXML
    private Label txtFailuresRate;

    @FXML
    private ProgressBar barFailuresRate;

    @FXML
    private GridPane ordersQuantityGrid1;

    @FXML
    private Label txtOrdersPercentage1;

    @FXML
    private Label txtQuantityPercentage1;

    @FXML
    private Label txtOrders1;

    @FXML
    private Label txtQuantity1;

    @FXML
    private ProgressBar barOrders1;

    @FXML
    private ProgressBar barQuantity1;

    @FXML
    private GridPane statistics2Grid1;

    @FXML
    private Label txtTotalIdleness1;

    @FXML
    private Label txtLostOrders1;

    @FXML
    private Label txtNoDelayOrders1;

    @FXML
    private Label txtInStockOrders1;

    @FXML
    private Button btnHome;
    

    @FXML
    void handleSimulation(ActionEvent event) {
    	//Style reset
    	this.simulationStartDate.setStyle(this.noError);
    	this.simulationEndDate.setStyle(this.noError);
    	this.cbYear.setStyle(this.noError);
    	this.txtLines.setStyle(this.noError);
    	this.txtWaitingDays.setStyle(this.noError);
    	
    	//All parameters set properly?
    	if(!this.checkInput())
    		return;
    	Integer parallelParts = null;
    	try {
    		parallelParts = Integer.parseInt(this.txtLines.getText());
    	}catch(Exception e) {
    		JOptionPane.showMessageDialog(null, "Ops! Set an integer number", "Lines wrong value", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	Integer maxWaitingDays = null;
    	try {
    		maxWaitingDays = Integer.parseInt(this.txtWaitingDays.getText());
    	}catch(Exception e) {
    		if(this.txtWaitingDays.getText().compareTo("")!=0) {	
    			this.txtWaitingDays.setStyle(this.error);
    			return;
    		}
    	}
    	
    	HashMap<String, Object> result = this.model.simulate(this.orderDate, this.orderMap, parallelParts, this.simulationStartDate.getValue(),
    			this.simulationEndDate.getValue(), this.cbYear.getValue(), maxWaitingDays, null);
    	
    	//Errors management
    	if(((String)result.get("errors")).compareTo("")!=0) {
    		String errors = "THE FOLLOWING ERRORS HAVE BEEN FOUND:\n"+(String)result.get("errors");
    		JOptionPane.showMessageDialog(null, errors,"ERRORS OCCURRED", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	
//    	System.out.println(result);
    	
    	//Enable some items
    	this.gridOrderDates.setDisable(false);
    	this.ordersQuantityGrid.setDisable(false);
    	this.statistics2Grid.setDisable(false);
    	this.txtNonProducedParts.setVisible(false);
    	this.listView.setPrefHeight(0);
    	this.listView.setVisible(false);
    	this.listView.getItems().clear();
    	
    	//ORDERS PRODUCTION Statistics
    	this.ordersQuantityGrid.setDisable(false);
    	int actualQuantity = (int)result.get("actualQuantity");
    	int totalQuantity = (int)result.get("totalQuantity");
    	int actualOrders = (int)result.get("actualOrders");
    	int totalOrders = (int)result.get("totalOrders");
    	double quantityPercentage = ((double)actualQuantity/totalQuantity)*100;
    	double ordersPercentage = ((double)actualOrders/totalOrders)*100;
    	this.txtQuantityPercentage.setText(String.format("%.1f%%", quantityPercentage));
    	this.txtOrdersPercentage.setText(String.format("%.1f%%", ordersPercentage));
    	this.barQuantity.setProgress(quantityPercentage/100);
    	this.barOrders.setProgress(ordersPercentage/100);
    	this.txtQuantity.setText(String.format("%d/%d pcs", actualQuantity, totalQuantity));
    	this.txtOrders.setText(String.format("%d/%d ord", actualOrders, totalOrders));
    	LocalDate start = (LocalDate)result.get("orderStart");
    	LocalDate end = (LocalDate)result.get("orderEnd");
    	if(start.equals(LocalDate.MIN)) {
    		this.txtStartDate.setText("Never started");
    		this.txtStartDate.setStyle("-fx-font-style: italic; ");
    		this.txtEndDate.setText("Never started");
    		this.txtEndDate.setStyle("-fx-font-style: italic; ");
    	}
    	else {
    		this.txtStartDate.setStyle("-fx-font-style: normal");
    		this.txtEndDate.setStyle("-fx-font-style: normal");
    		this.txtStartDate.setText(start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    		this.txtEndDate.setText(end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    	}
    	
    	//COMPLETE PERIOD Statistics
    	this.txtLostOrders.setText(""+(int)result.get("lostOrders")+" ord");
    	this.txtInStockOrders.setText(""+(int)result.get("inStockOrders")+" ord");
    	this.txtTotalIdleness.setText(""+(int)result.get("totalIdleness")+" days");
    	this.txtNoDelayOrders.setText((int)result.get("inTimeOrders")+" ord");
    	@SuppressWarnings("unchecked")
		ArrayList<Order> missingParts = new ArrayList<Order>((ArrayList<Order>)result.get("newOrder"));
    	if(missingParts.size()!=0) {
    		this.txtNonProducedParts.setVisible(true);
    		this.listView.setPrefHeight(30*missingParts.size());
    		if(this.listView.getPrefHeight()>100)
    			this.listView.setPrefHeight(100);
    		this.listView.getItems().addAll(missingParts);
    		this.listView.setVisible(true);
    	}
    	
    }
    
    @FXML
    void handleSimulationPlus(ActionEvent event) {
    	//Style reset
    	this.simulationStartDate1.setStyle(this.noError);
    	this.simulationEndDate1.setStyle(this.noError);
    	this.cbYear1.setStyle(this.noError);
    	this.txtLines1.setStyle(this.noError);
    	this.txtWaitingDays1.setStyle(this.noError);
    	this.txtSimulations.setStyle(this.noError);
    	
    	//All parameters set properly?
    	if(!this.checkInput())
    		return;
    	Integer parallelParts = null;
    	try {
    		parallelParts = Integer.parseInt(this.txtLines.getText());
    	}catch(Exception e) {
    		JOptionPane.showMessageDialog(null, "Ops! Set an integer number", "Lines wrong value", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	Integer maxWaitingDays = null;
    	try {
    		maxWaitingDays = Integer.parseInt(this.txtWaitingDays.getText());
    	}catch(Exception e) {
    		if(this.txtWaitingDays.getText().compareTo("")!=0) {	
    			this.txtWaitingDays.setStyle(this.error);
    			return;
    		}
    	}
    	Integer simulationsNumber = null;
    	try {
    		simulationsNumber = Integer.parseInt(this.txtSimulations.getText());
    		if(simulationsNumber < 2)
    			throw new Exception();
    	}catch(Exception e) {
    		this.txtSimulations.setStyle(this.error);
    		return;
    	}
    	
    	//simulation
    	HashMap<String, Object> result = this.model.simulate(this.orderDate, this.orderMap, parallelParts, this.simulationStartDate.getValue(),
    			this.simulationEndDate.getValue(), this.cbYear.getValue(), maxWaitingDays, simulationsNumber);
    	
    	//Errors management
    	if(((String)result.get("errors")).compareTo("")!=0) {
    		String errors = "THE FOLLOWING ERRORS HAVE BEEN FOUND:\n"+(String)result.get("errors");
    		JOptionPane.showMessageDialog(null, errors,"ERRORS OCCURRED", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
  
    	
    	//Enable grids
    	this.gridSuccessRate.setDisable(false);
    	this.gridIncompleteRate.setDisable(false);
    	this.gridFailuresRate.setDisable(false);
    	this.ordersQuantityGrid1.setDisable(false);
    	this.statistics2Grid1.setDisable(false);
    	
    	//ORDERS PRODUCTION Statistics
    	this.barSuccessRate.setProgress((double)result.get("success"));
    	this.barIncompleteRate.setProgress((double)result.get("incomplete"));
    	this.barFailuresRate.setProgress((double)result.get("failures"));
    	this.txtSuccessRate.setText(String.format("%.1f%%", (double)result.get("success")*100));
    	this.txtIncompleteRate.setText(String.format("%.1f%%", (double)result.get("incomplete")*100));
    	this.txtFailuresRate.setText(String.format("%.1f%%", (double)result.get("failures")*100));
    	
    	//COMPLETE PERIOD Statistics
    	this.barOrders1.setProgress((double)result.get("actualOrders")/(int)result.get("totalOrders"));
    	this.txtOrdersPercentage1.setText(String.format("%.1f%%", 100*(double)result.get("actualOrders")/(int)result.get("totalOrders")));
    	this.txtOrders1.setText(String.format("%.1f/%d", (double)result.get("actualOrders"), (int)result.get("totalOrders")));
    	this.barQuantity1.setProgress((double)result.get("actualQuantity")/(int)result.get("totalQuantity"));
    	this.txtQuantityPercentage1.setText(String.format("%.1f%%", 100*(double)result.get("actualQuantity")/(int)result.get("totalQuantity")));
    	this.txtQuantity1.setText(String.format("%.1f/%d", (double)result.get("actualQuantity"), (int)result.get("totalQuantity")));
    	
    	this.txtLostOrders1.setText(String.format("%.1f ord", (double)result.get("lostOrders")));
    	this.txtInStockOrders1.setText(String.format("%.1f ord", (double)result.get("inStockOrders")));
    	this.txtTotalIdleness1.setText(String.format("%.1f days", (double)result.get("totalIdleness")));
    	this.txtNoDelayOrders1.setText(String.format("%.1f ord", (double)result.get("inTimeOrders")));
    }

    @FXML
    void initialize() {
    	assert simulationTop != null : "fx:id=\"simulationTop\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert vboxMain != null : "fx:id=\"vboxMain\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert simulationStartDate != null : "fx:id=\"simulationStartDate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert simulationEndDate != null : "fx:id=\"simulationEndDate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert cbYear != null : "fx:id=\"cbYear\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtLines != null : "fx:id=\"txtLines\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtWaitingDays != null : "fx:id=\"txtWaitingDays\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert btnSimulate != null : "fx:id=\"btnSimulate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtStartDate != null : "fx:id=\"txtStartDate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtEndDate != null : "fx:id=\"txtEndDate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtNonProducedParts != null : "fx:id=\"txtNonProducedParts\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert listView != null : "fx:id=\"listView\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert ordersQuantityGrid != null : "fx:id=\"ordersQuantityGrid\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtOrdersPercentage != null : "fx:id=\"txtOrdersPercentage\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtQuantityPercentage != null : "fx:id=\"txtQuantityPercentage\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtOrders != null : "fx:id=\"txtOrders\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtQuantity != null : "fx:id=\"txtQuantity\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert barOrders != null : "fx:id=\"barOrders\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert barQuantity != null : "fx:id=\"barQuantity\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert statistics2Grid != null : "fx:id=\"statistics2Grid\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtTotalIdleness != null : "fx:id=\"txtTotalIdleness\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtLostOrders != null : "fx:id=\"txtLostOrders\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtNoDelayOrders != null : "fx:id=\"txtNoDelayOrders\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtInStockOrders != null : "fx:id=\"txtInStockOrders\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert vboxMain1 != null : "fx:id=\"vboxMain1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert simulationStartDate1 != null : "fx:id=\"simulationStartDate1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert simulationEndDate1 != null : "fx:id=\"simulationEndDate1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert cbYear1 != null : "fx:id=\"cbYear1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtLines1 != null : "fx:id=\"txtLines1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtWaitingDays1 != null : "fx:id=\"txtWaitingDays1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtSimulations != null : "fx:id=\"txtSimulations\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert btnSimulate1 != null : "fx:id=\"btnSimulate1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert gridSuccessRate != null : "fx:id=\"gridSuccessRate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtSuccessRate != null : "fx:id=\"txtSuccessRate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert barSuccessRate != null : "fx:id=\"barSuccessRate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert gridIncompleteRate != null : "fx:id=\"gridIncompleteRate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtIncompleteRate != null : "fx:id=\"txtIncompleteRate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert barIncompleteRate != null : "fx:id=\"barIncompleteRate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert gridFailuresRate != null : "fx:id=\"gridFailuresRate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtFailuresRate != null : "fx:id=\"txtFailuresRate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert barFailuresRate != null : "fx:id=\"barFailuresRate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert ordersQuantityGrid1 != null : "fx:id=\"ordersQuantityGrid1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtOrdersPercentage1 != null : "fx:id=\"txtOrdersPercentage1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtQuantityPercentage1 != null : "fx:id=\"txtQuantityPercentage1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtOrders1 != null : "fx:id=\"txtOrders1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtQuantity1 != null : "fx:id=\"txtQuantity1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert barOrders1 != null : "fx:id=\"barOrders1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert barQuantity1 != null : "fx:id=\"barQuantity1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert statistics2Grid1 != null : "fx:id=\"statistics2Grid1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtTotalIdleness1 != null : "fx:id=\"txtTotalIdleness1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtLostOrders1 != null : "fx:id=\"txtLostOrders1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtNoDelayOrders1 != null : "fx:id=\"txtNoDelayOrders1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtInStockOrders1 != null : "fx:id=\"txtInStockOrders1\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert btnHome != null : "fx:id=\"btnHome\" was not injected: check your FXML file 'Simulation.fxml'.";

        this.simulationTop.setStyle("-fx-background-color: rgb(33, 215, 243);");
        this.txtNonProducedParts.setVisible(false);
        //hide the list view at the beginning
        this.listView.setPrefHeight(0);
        //Binds the two pages to make the data insertion easier (once instead of twice)
        this.simulationStartDate.valueProperty().bindBidirectional(this.simulationStartDate1.valueProperty());
        this.simulationEndDate.valueProperty().bindBidirectional(this.simulationEndDate1.valueProperty());
        this.cbYear.valueProperty().bindBidirectional(this.cbYear1.valueProperty());
        this.txtLines.textProperty().bindBidirectional(this.txtLines1.textProperty());
        this.txtWaitingDays.textProperty().bindBidirectional(this.txtWaitingDays1.textProperty());
    }

	public void setOrder(ThesisController home, ThesisModel model, HashMap<String, Integer> orderMap, LocalDate orderDate) {
		this.home = home;
		this.model = model;
		this.orderMap = orderMap;
		this.orderDate = orderDate;
		//Add years to the combo box
        for(int i=this.model.getMIN_YEAR(); i<=this.model.getMAX_YEAR(); i++) {
        	this.cbYear.getItems().add(i);
        	this.cbYear1.getItems().add(i);
        }
        //Disable all the dates after the order date (simulation dates must contain the order date)
        this.simulationStartDate.setDayCellFactory(getDayCellFactory(LocalDate.ofYearDay(this.orderDate.getYear(),1), true));
        this.simulationStartDate1.setDayCellFactory(getDayCellFactory(LocalDate.ofYearDay(this.orderDate.getYear(), 1), true));
	}
	
	@FXML
    void goBackHome(ActionEvent event) {
		this.home.returnToPrimary();
    }
	
	/**
	 * If the start date is set --> enables the end date but disables all the dates before the start date and the order date
	 */
	@FXML
    void manageEndDate(ActionEvent event) {
		if(this.simulationStartDate.getValue() == null || this.simulationStartDate1.getValue() == null) {
			this.simulationEndDate.setValue(null);
			this.simulationEndDate.setDisable(true);
			this.simulationEndDate1.setValue(null);
			this.simulationEndDate1.setDisable(true);
			return;
		}
		this.simulationEndDate.setDisable(false);
		this.simulationEndDate.setDayCellFactory(getDayCellFactory(this.simulationStartDate.getValue(), false));
		this.simulationEndDate1.setDisable(false);
		this.simulationEndDate1.setDayCellFactory(getDayCellFactory(this.simulationStartDate1.getValue(), false));
    }
	
	/**
	 * Checks if the current input is valid, otherwise set the style (red border) to indicate the error
	 * @return {@code true} if everything is ok, {@code false} else
	 */
	private boolean checkInput() {
		if(this.simulationStartDate.getValue() == null || this.simulationStartDate1.getValue() == null) {
    		this.simulationStartDate.setStyle(this.error);
    		this.simulationStartDate1.setStyle(this.error);
    		return false;
    	} else if(this.simulationEndDate.getValue() == null || this.simulationEndDate.getValue().isBefore(this.simulationStartDate.getValue()) ||
    			this.simulationEndDate1.getValue() == null || this.simulationEndDate1.getValue().isBefore(this.simulationStartDate1.getValue())) {
    		this.simulationEndDate.setStyle(this.error);
    		this.simulationEndDate1.setStyle(this.error);
    		return false;
    	} else if(this.cbYear.getValue() == null || this.cbYear1.getValue() == null) {
    		this.cbYear.setStyle(this.error);
    		this.cbYear1.setStyle(this.error);
    		return false;
    	} else if(this.txtLines.getText().compareTo("")==0 || this.txtLines1.getText().compareTo("")==0) {
    		this.txtLines.setStyle(this.error);
    		this.txtLines1.setStyle(this.error);
    		return false;
    	}
		return true;
	}
	
	// Factory to create Cell of DatePicker -- start = true if the CellFactory is for the start date, false else
    public Callback<DatePicker, DateCell> getDayCellFactory(LocalDate year, boolean start) {
 
        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
 
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if(start) {
		                    if (item.isBefore(year) || item.isAfter(orderDate) || item.isBefore(orderDate.minusYears(1))){
		                       setDisable(true);
		                       setStyle("-fx-background-color: #ffc0cb;");
		                    }
                        }else {
		                    if (item.isBefore(orderDate) || item.isAfter(year.plusYears(1).minusDays(1)) || item.isAfter(orderDate.plusYears(1).minusDays(1))){
		                       setDisable(true);
		                       setStyle("-fx-background-color: #ffc0cb;");
		                    }
                        }
                    }
                };
            }
        };
        return dayCellFactory;
    }
    
}
