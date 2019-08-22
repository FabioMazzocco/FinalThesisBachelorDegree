package it.polito.s234844.thesis;

import java.util.ArrayList;
import java.util.HashMap;
import it.polito.s234844.thesis.model.ThesisModel;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import org.apache.commons.math3.distribution.NormalDistribution;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.scene.control.ToggleButton;

public class DueDateQuotingController {

	private ThesisModel model;
	private ThesisController home;
	private HashMap<String, Integer> orderMap;
	private LocalDate orderDate;
//	private HashMap<String, Part> partsMap;
	private boolean isParallel;
	
	@FXML
    private ResourceBundle resources;

    @FXML
    private URL location;
    
    @FXML
    private Label dueDateQuotingTop;
    
    @FXML
    private Slider dueDateQuotingSlider;
    
    @FXML
    private TextField txtProbability;

    @FXML
    private ToggleButton btnSerialParallelProduction;
    
    @FXML
    private Label txtSerialProduction;

    @FXML
    private Label txtParallelProduction;
    
    @FXML
    private Button btnCalculate;
    
    @FXML
    private Text txtDays;

    @FXML
    private Text txtDate;
    
    @FXML
    private LineChart<String, Double> dueDateQuotingChart;
    
    @FXML
    private Button btnHome;
    
    public DueDateQuotingController() {
    	this.model = null;
    	this.orderMap = null;
//    	this.partsMap = null;
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
    void handleDueDateQuotation(ActionEvent event) {
    	HashMap<String, Object> result = this.model.dueDateQuoting(this.orderMap, this.orderDate, this.dueDateQuotingSlider.getValue()/100, this.isParallel);
    	if(((String)result.get("errors")).compareTo("")!=0)
    		System.out.println((String)result.get("errors"));
    	else {
    		this.txtDays.setText(""+(Integer) result.get("days"));
    		this.txtDate.setText(this.orderDate.plusDays((Integer)result.get("days")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    		this.setChart((NormalDistribution)result.get("normal"));
    	}
    }
    
    @FXML
    void changeProbability(ActionEvent event) {
    	try {
    		Double newProbability = Double.parseDouble(this.txtProbability.getText());
    		if(newProbability<=0 || newProbability>=1)
    			throw new Exception();
    		this.dueDateQuotingSlider.setValue(newProbability);
    		this.txtProbability.setText(newProbability+"%");
    	}catch(Exception e) {
    		this.txtProbability.clear();
    		this.txtProbability.setPromptText("0.00%");
    	}
    }
    
    @FXML
    void initialize() {
        assert dueDateQuotingSlider != null : "fx:id=\"dueDateQuotingSlider\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
        assert btnSerialParallelProduction != null : "fx:id=\"btnSerialParallelProduction\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
        assert dueDateQuotingChart != null : "fx:id=\"dueDateQuotingChart\" was not injected: check your FXML file 'DueDateQuoting.fxml'.";
        this.txtProbability.textProperty().bind(
                Bindings.format(
                    "%.2f%%",
                    this.dueDateQuotingSlider.valueProperty()
                )
            );
    }
    
    @FXML
    void goBackHome(ActionEvent event) {
    	this.home.returnToPrimary();
    }

	public void setOrder(ThesisController home, ThesisModel model, HashMap<String, Integer> orderMap, LocalDate orderDate) {
		this.home = home;
		this.model = model;
		this.orderMap = orderMap;
		this.orderDate = orderDate;
	}
	
	public void setChart(NormalDistribution normal) {
		this.dueDateQuotingChart.getData().clear();
		

        XYChart.Series<String, Double> serie= new XYChart.Series<String, Double>();
        
		for(double i=0; i<normal.inverseCumulativeProbability(0.9999999)+1; i++) {
			XYChart.Data<String, Double> data = new XYChart.Data<String, Double>(Double.toString(i), normal.density(i));
			
			serie.getData().add(data);
		}
		this.dueDateQuotingChart.getData().add(serie);
	}

}

