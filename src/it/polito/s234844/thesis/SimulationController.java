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
    private GridPane ordersQuantityGrid;
    
    @FXML
    private ProgressBar barOrders;

    @FXML
    private ProgressBar barQuantity;

    @FXML
    private Label txtOrdersPercentage;

    @FXML
    private Label txtQuantityPercentage;

    @FXML
    private Label txtOrders;

    @FXML
    private Label txtQuantity;
    
    @FXML
    private GridPane statisticsGrid;

    @FXML
    private Label txtStartDate;

    @FXML
    private Label txtEndDate;

    @FXML
    private Label txtLostOrders;

    @FXML
    private Label txtInStockOrders;

    @FXML
    private Label txtTotalIdleness;

    @FXML
    private Label txtNoDelayOrders;
    
    @FXML
    private ListView<Order> listView;
    
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
    	if(this.simulationStartDate.getValue() == null) {
    		this.simulationStartDate.setStyle(this.error);
    		return;
    	} else if(this.simulationEndDate.getValue() == null || this.simulationEndDate.getValue().isBefore(this.simulationStartDate.getValue())) {
    		this.simulationEndDate.setStyle(this.error);
    		return;
    	} else if(this.cbYear.getValue() == null) {
    		this.cbYear.setStyle(this.error);
    		return;
    	} else if(this.txtLines.getText().compareTo("")==0) {
    		this.txtLines.setStyle(this.error);
    		return;
    	}
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
    			this.simulationEndDate.getValue(), this.cbYear.getValue(), maxWaitingDays);
    	
    	//Errors management
    	if(((String)result.get("errors")).compareTo("")!=0) {
    		String errors = "THE FOLLOWING ERRORS HAVE BEEN FOUND:\n"+(String)result.get("errors");
    		JOptionPane.showMessageDialog(null, errors,"ERRORS OCCURRED", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	
    	System.out.println(result); //To be deleted
    	
    	this.listView.setPrefHeight(0);
    	this.listView.setVisible(false);
    	this.listView.getItems().clear();
    	
    	//Statistical values
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
    	this.txtOrders.setText(String.format("%d/%d ords", actualOrders, totalOrders));
    	LocalDate start = (LocalDate)result.get("orderStart");
    	LocalDate end = (LocalDate)result.get("orderEnd");
    	if(start.equals(LocalDate.MIN)) {
    		this.txtStartDate.setText("Never started");
    		this.txtEndDate.setText("Never started");
    	}
    	else {
    		this.txtStartDate.setText(start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    		this.txtEndDate.setText(end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    	}
    	this.txtLostOrders.setText(""+(int)result.get("lostOrders")+" ord");
    	this.txtInStockOrders.setText(""+(int)result.get("inStockOrders")+" ord");
    	this.txtTotalIdleness.setText(""+(long)result.get("totalIdleness")+" days");
    	this.txtNoDelayOrders.setText((int)result.get("inTimeOrders")+" ord");
    	@SuppressWarnings("unchecked")
		ArrayList<Order> missingParts = new ArrayList<Order>((ArrayList<Order>)result.get("newOrder"));
    	if(missingParts.size()!=0) {
    		this.listView.setPrefHeight(100);
    		this.listView.getItems().addAll(missingParts);
    		this.listView.setVisible(true);
    	}
    	
    }

    @FXML
    void initialize() {
    	assert simulationTop != null : "fx:id=\"simulationTop\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert simulationStartDate != null : "fx:id=\"simulationStartDate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert simulationEndDate != null : "fx:id=\"simulationEndDate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert cbYear != null : "fx:id=\"cbYear\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtLines != null : "fx:id=\"txtLines\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtWaitingDays != null : "fx:id=\"txtWaitingDays\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert btnSimulate != null : "fx:id=\"btnSimulate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert ordersQuantityGrid != null : "fx:id=\"ordersQuantityGrid\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert barOrders != null : "fx:id=\"barOrders\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert barQuantity != null : "fx:id=\"barQuantity\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtOrdersPercentage != null : "fx:id=\"txtOrdersPercentage\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtQuantityPercentage != null : "fx:id=\"txtQuantityPercentage\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtOrders != null : "fx:id=\"txtOrders\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtQuantity != null : "fx:id=\"txtQuantity\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert statisticsGrid != null : "fx:id=\"statisticsGrid\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtStartDate != null : "fx:id=\"txtStartDate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtEndDate != null : "fx:id=\"txtEndDate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtLostOrders != null : "fx:id=\"txtLostOrders\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtInStockOrders != null : "fx:id=\"txtInStockOrders\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtTotalIdleness != null : "fx:id=\"txtTotalIdleness\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert txtNoDelayOrders != null : "fx:id=\"txtNoDelayOrders\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert btnHome != null : "fx:id=\"btnHome\" was not injected: check your FXML file 'Simulation.fxml'.";
        this.simulationTop.setStyle("-fx-background-color: rgb(33, 215, 243);");
        this.listView.setPrefHeight(0);
    }

	public void setOrder(ThesisController home, ThesisModel model, HashMap<String, Integer> orderMap, LocalDate orderDate) {
		this.home = home;
		this.model = model;
		this.orderMap = orderMap;
		this.orderDate = orderDate;
		
        for(int i=this.model.getMIN_YEAR(); i<=this.model.getMAX_YEAR(); i++) {
        	this.cbYear.getItems().add(i);
        }
        
        this.simulationStartDate.setDayCellFactory(getDayCellFactory(LocalDate.ofYearDay(this.orderDate.getYear(),1), true));
	}
	
	@FXML
    void goBackHome(ActionEvent event) {
		this.home.returnToPrimary();
    }
	
	@FXML
    void manageEndDate(ActionEvent event) {
		if(this.simulationStartDate.getValue() == null) {
			this.simulationEndDate.setValue(null);
			this.simulationEndDate.setDisable(true);
			return;
		}
		this.simulationEndDate.setDisable(false);
		this.simulationEndDate.setDayCellFactory(getDayCellFactory(this.simulationStartDate.getValue(), false));
    }
	
	// Factory to create Cell of DatePicker
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
