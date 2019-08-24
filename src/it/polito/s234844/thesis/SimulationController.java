package it.polito.s234844.thesis;

import java.net.URL;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.ResourceBundle;

import it.polito.s234844.thesis.model.ThesisModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
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
    private ComboBox<Year> cbYear;

    @FXML
    private Button btnSimulate;
    
    @FXML
    private Button btnHome;

    @FXML
    void handleSimulation(ActionEvent event) {
    	//Style reset
    	this.simulationStartDate.setStyle(this.noError);
    	this.simulationEndDate.setStyle(this.noError);
    	this.cbYear.setStyle(this.noError);
    	
    	//All parameters set?
    	if(this.simulationStartDate.getValue() == null) {
    		this.simulationStartDate.setStyle(this.error);
    		return;
    	} else if(this.simulationEndDate.getValue() == null) {
    		this.simulationEndDate.setStyle(this.error);
    		return;
    	} else if(this.cbYear.getValue() == null) {
    		this.cbYear.setStyle(this.error);
    		return;
    	}
    		
    }

    @FXML
    void initialize() {
        assert simulationTop != null : "fx:id=\"simulationTop\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert simulationStartDate != null : "fx:id=\"simulationStartDate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert simulationEndDate != null : "fx:id=\"simulationEndDate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert cbYear != null : "fx:id=\"cbYear\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert btnSimulate != null : "fx:id=\"btnSimulate\" was not injected: check your FXML file 'Simulation.fxml'.";
        assert btnHome != null : "fx:id=\"btnHome\" was not injected: check your FXML file 'Simulation.fxml'.";
        this.simulationTop.setStyle("-fx-background-color: rgb(33, 215, 243);");
    }

	public void setOrder(ThesisController home, ThesisModel model, HashMap<String, Integer> orderMap, LocalDate orderDate) {
		this.home = home;
		this.model = model;
		this.orderMap = orderMap;
		this.orderDate = orderDate;
		
        for(int i=this.model.getMIN_YEAR(); i<=this.model.getMAX_YEAR(); i++) {
        	this.cbYear.getItems().add(Year.of(i));
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
