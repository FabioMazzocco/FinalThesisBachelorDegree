package it.polito.s234844.thesis;

import java.util.HashMap;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import it.polito.s234844.thesis.model.ThesisModel;

public class DueDateProbabilityController {
	private ThesisController home;
	private ThesisModel thesisMmodel;
	private HashMap<String, Integer> orderMap;
	private LocalDate orderDate;
	private boolean isParallel;
	
	private final String bold = "-fx-font-weight: bold;";
	private final String normal = "-fx-font-weight: 400";
	
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private DatePicker dueDatePicker;

    @FXML
    private Button btnCalculate;
    
    @FXML
    private Button btnSerialParallelProduction;

    @FXML
    private Label txtSerialProduction;

    @FXML
    private Label txtParallelProduction;

    @FXML
    private Text txtProbability;

    @FXML
    private AreaChart<String, Double> dueDateProbabilityChart;

    @FXML
    private Button btnHome;

    @FXML
    private Pane dueDateProbabilityTop;

    @FXML
    void handleDueDateProability(ActionEvent event) {
    	if(this.dueDatePicker.getValue() == null)
    		this.dueDatePicker.setValue(this.orderDate);
    	HashMap<String, Object> result = this.thesisMmodel.dueDateProbability(this.orderMap, this.orderDate, this.dueDatePicker.getValue(), this.isParallel);
    	//Errors management
    	if(((String)result.get("errors")).compareTo("")!=0) {
    		String errors = "THE FOLLOWING ERRORS HAVE BEEN FOUND:\n"+(String)result.get("errors");
    		JOptionPane.showMessageDialog(null, errors,"ERRORS OCCURRED", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	
    	this.txtProbability.setText(String.format("%.2f%%", (Double)result.get("probability")*100));
    }
    
    @FXML
    void handleSerialParallelProduction(ActionEvent event) {
    	if(this.isParallel) {
    		this.isParallel = false;
    		this.txtParallelProduction.setDisable(true);
    		this.txtParallelProduction.setStyle(this.normal);
    		this.txtSerialProduction.setDisable(false);
    		this.txtSerialProduction.setStyle(this.bold);
    		this.btnSerialParallelProduction.setText("Serial production");
    	}else {
    		this.isParallel = true;
    		this.txtParallelProduction.setDisable(false);
    		this.txtParallelProduction.setStyle(this.bold);
    		this.txtSerialProduction.setDisable(true);
    		this.txtSerialProduction.setStyle(this.normal);
    		this.btnSerialParallelProduction.setText("Parallel production");
    	}
    }

    @FXML
    void initialize() {
    	assert dueDatePicker != null : "fx:id=\"dueDatePicker\" was not injected: check your FXML file 'DueDateProbability.fxml'.";
        assert btnCalculate != null : "fx:id=\"btnCalculate\" was not injected: check your FXML file 'DueDateProbability.fxml'.";
        assert btnSerialParallelProduction != null : "fx:id=\"btnSerialParallelProduction\" was not injected: check your FXML file 'DueDateProbability.fxml'.";
        assert txtSerialProduction != null : "fx:id=\"txtSerialProduction\" was not injected: check your FXML file 'DueDateProbability.fxml'.";
        assert txtParallelProduction != null : "fx:id=\"txtParallelProduction\" was not injected: check your FXML file 'DueDateProbability.fxml'.";
        assert txtProbability != null : "fx:id=\"txtProbability\" was not injected: check your FXML file 'DueDateProbability.fxml'.";
        assert dueDateProbabilityChart != null : "fx:id=\"dueDateProbabilityChart\" was not injected: check your FXML file 'DueDateProbability.fxml'.";
        assert btnHome != null : "fx:id=\"btnHome\" was not injected: check your FXML file 'DueDateProbability.fxml'.";
        assert dueDateProbabilityTop != null : "fx:id=\"dueDateProbabilityTop\" was not injected: check your FXML file 'DueDateProbability.fxml'.";
        this.dueDateProbabilityTop.setStyle("-fx-background-color: rgb(33, 215, 243);");
        this.dueDatePicker.setDayCellFactory(getDayCellFactory());
        this.dueDatePicker.setStyle("-fx-text-align: center;");
        this.btnHome.setStyle("-fx-font-weight: bold");
        this.isParallel = false;
    }

	public void setOrder(ThesisController home, ThesisModel model, HashMap<String, Integer> orderMap, LocalDate orderDate) {
		this.home = home;
		this.thesisMmodel = model;
		this.orderMap = orderMap;
		this.orderDate = orderDate;
	}
	
	@FXML
    void goBackHome(ActionEvent event) {
		this.home.returnToPrimary();
    }
	
	// Factory to create Cell of DatePicker
    public Callback<DatePicker, DateCell> getDayCellFactory() {
 
        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
 
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
 
                        // Disable all dates before today
                        if (item.isBefore(orderDate)){
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };
        return dayCellFactory;
    }
}
