package it.polito.s234844.thesis;

import java.util.HashMap;

import it.polito.s234844.thesis.model.Part;
import it.polito.s234844.thesis.model.ThesisModel;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.DragEvent;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.control.ToggleButton;

public class DueDateQuotingController {

	private ThesisModel model;
	private HashMap<String, Integer> orderMap;
	private HashMap<String, Part> partsMap;
	private boolean isParallel;
	
	@FXML
    private ResourceBundle resources;

    @FXML
    private URL location;
    
    @FXML
    private Slider dueDateQuotingSlider;

    @FXML
    private ToggleButton btnSerialParallelProduction;
    
    @FXML
    private Label txtSerialProduction;

    @FXML
    private Label txtParallelProduction;
    
    @FXML
    private Text txtDays;

    @FXML
    private Text txtDate;
    
    @FXML
    private AreaChart<Double, Double> dueDateQuotingChart;
    
    public DueDateQuotingController() {
    	this.model = null;
    	this.orderMap = null;
    	this.partsMap = null;
    	this.isParallel = false;
    }
    
    @FXML
    void handleSerialParallelProduction(ActionEvent event) {
    	if(this.isParallel) {
    		this.isParallel = false;
    		this.txtParallelProduction.setDisable(true);
    		this.txtSerialProduction.setDisable(false);
    		this.btnSerialParallelProduction.setText("Serial production");
    	}else {
    		this.isParallel = true;
    		this.txtParallelProduction.setDisable(false);
    		this.txtSerialProduction.setDisable(true);
    		this.btnSerialParallelProduction.setText("Parallel production");
    	}
    }

    
    @FXML
    void handleDueDateQuotation(DragEvent event) {
    	
    }
    
    @FXML
    void initialize() {
        assert dueDateQuotingSlider != null : "fx:id=\"dueDateQuotingSlider\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
        assert btnSerialParallelProduction != null : "fx:id=\"btnSerialParallelProduction\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
        assert dueDateQuotingChart != null : "fx:id=\"dueDateQuotingChart\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";

    }

	public void setOrder(ThesisModel model, HashMap<String, Integer> orderMap) {
		this.model = model;
		this.orderMap = orderMap;
		
//		for(double i=0; i<300; i=i+0,001) {
//			this.dueDateQuotingChart.getData().add()
//		}
	}

}

